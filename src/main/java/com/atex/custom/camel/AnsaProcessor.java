package com.atex.custom.camel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.camel.component.file.GenericFile;
import org.apache.commons.io.IOUtils;

import com.atex.custom.parser.AnsaParser;
import com.atex.custom.parser.AnsaParser.AnsaItaliaMondoInfo;
import com.atex.onecms.app.dam.standard.aspects.OneArticleBean;
import com.atex.onecms.app.dam.standard.aspects.OneContentBean;
import com.atex.onecms.app.dam.standard.aspects.OneImageBean;
import com.atex.onecms.content.ContentFileInfo;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.ContentWriteBuilder;
import com.atex.onecms.content.FilesAspectBean;
import com.atex.onecms.content.InsertionInfoAspectBean;
import com.atex.onecms.content.OneCMSAspectBean;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.files.FileInfo;
import com.atex.onecms.content.metadata.MetadataInfo;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.standard.image.exif.MetadataTags;
import com.polopoly.metadata.Metadata;
import com.polopoly.util.StringUtil;

public class AnsaProcessor extends BaseProcessor {

	private static Subject SYSTEM_SUBJECT = new Subject("98", null);

	protected final Logger LOG = Logger.getLogger(getClass().getName());

	private static final Pattern DOUBLE_SLASH_PATTERN = Pattern.compile("/*/");
	public static final String SCHEME_TMP = "tmp";

	@Override
	protected void processFile(GenericFile file) throws Exception {
		BufferedInputStream bis = null;
		try {
			checkFileStability(file);

			String filePath = file.getAbsoluteFilePath();

			bis = new BufferedInputStream(new FileInputStream(filePath));


			Class<?> c = Class.forName(getParserClass());
			AnsaParser parser = (AnsaParser) c.newInstance();
			AnsaItaliaMondoInfo ansaItaliaMondoInfo = parser.parseFile(bis);


			// contentData
			final OneContentBean oneContentBean = ansaItaliaMondoInfo.getBean();//parser.parseFile(new File(filePath));

			// add p.InsertionInfo
			String securityParent = getSecurityParent();
			if (StringUtil.isEmpty(securityParent)) {
				securityParent = ansaItaliaMondoInfo.getSecurityParent();
			}
			ContentId securityParentContentId = contentManager.resolve(securityParent, Subject.NOBODY_CALLER).getContentId();
			final InsertionInfoAspectBean insertionInfoAspectBean = new InsertionInfoAspectBean(securityParentContentId);

			//Immagine
			for(String fileImage : ansaItaliaMondoInfo.getFileImages()) {
				String imageFilePath = filePath.substring(0,filePath.lastIndexOf(File.separator)) + File.separator + fileImage;

				ContentId imageContentId = createImage(imageFilePath, fileImage, securityParentContentId);
				if (imageContentId != null) {
					if (((OneArticleBean) oneContentBean).getImages() == null) {
						((OneArticleBean) oneContentBean).setImages(new ArrayList<ContentId>());
					}
					((OneArticleBean) oneContentBean).getImages().add(imageContentId);
				}
			}

			// add atex.metadata
			final MetadataInfo metadataInfo = new MetadataInfo();
			Set<String> set = new HashSet<>();
			set.add("p.StandardCategorization");
			metadataInfo.setTaxonomyIds(set);

			metadataInfo.setMetadata(ansaItaliaMondoInfo.getMetadata());


			final ContentWriteBuilder<OneContentBean> cwb = createContentWriteBuilder(oneContentBean);

			cwb.aspect("p.InsertionInfo", insertionInfoAspectBean);
			cwb.aspect("atex.Metadata", metadataInfo);

			final ContentWrite<OneContentBean> content = cwb.buildCreate();
			ContentResult<Object> cr = getContentManager().create(content, SYSTEM_SUBJECT);

			if (!cr.getStatus().isOk()) {
				LOG.severe("Error importing file: " + filePath + "." + cr.getStatus().toString());
			}else {
				LOG.info("Inserted content with contentid: " + cr.getContentId().getContentId() + " from file:" + filePath);
			}

		} finally {
			IOUtils.closeQuietly(bis);
		}
	}

	private ContentId createImage(String imageFilePath, String imageFilename, ContentId securityParentContentId) throws Exception {
		ContentId imageContentId = null;
		final OneImageBean damFiegWireImageAspectBean = new OneImageBean();

		BufferedInputStream bis = null;
		try {
			File f = new File(imageFilePath);
			if(!f.exists() || f.isDirectory()) {
				String message = "Cannot find jpg file: " + imageFilePath;
				throw new FileNotFoundException(message);
			}

			bis = new BufferedInputStream(new FileInputStream(imageFilePath));
			MetadataTags metadataTags = MetadataTags.extract(bis);
			bis.close();

			bis = new BufferedInputStream(new FileInputStream(imageFilePath));
			//CustomMetadataTags customMetadataTags = extract(bis);
			bis.close();

			/*
			 * Upload file to File Storage Server
			 */
			bis = new BufferedInputStream(new FileInputStream(imageFilePath));
			String cleanPath = cleanPath(imageFilename.toString());
			String mimeType = getFormatName(bis);
			bis.close();

			bis = new BufferedInputStream(new FileInputStream(imageFilePath));
			FileInfo fInfo = getFileService().uploadFile(SCHEME_TMP, null, cleanPath, bis, mimeType, SYSTEM_SUBJECT);
			bis.close();

			/*
			 * create Json file content
			 */

			// atex.Files
			FilesAspectBean filesAspectBean = new FilesAspectBean();
			ContentFileInfo contentFileInfo = new ContentFileInfo(fInfo.getOriginalPath(), fInfo.getURI());
			HashMap<String, ContentFileInfo> files = new HashMap<String, ContentFileInfo>();
			files.put(fInfo.getOriginalPath(), contentFileInfo);
			filesAspectBean.setFiles(files);

			// atex.Image
			ImageInfoAspectBean imageInfoAspectBean = new ImageInfoAspectBean();
			imageInfoAspectBean.setHeight(metadataTags.getImageHeight());
			imageInfoAspectBean.setWidth(metadataTags.getImageWidth());
			imageInfoAspectBean.setFilePath(fInfo.getOriginalPath());

			// contentData
			damFiegWireImageAspectBean.setWidth(metadataTags.getImageWidth());
			damFiegWireImageAspectBean.setHeight(metadataTags.getImageHeight());

			// add p.InsertionInfo
			final InsertionInfoAspectBean insertionInfoAspectBean = new InsertionInfoAspectBean(securityParentContentId);

			// add atex.metadata
			final MetadataInfo metadataInfo = new MetadataInfo();
			Set<String> set = new HashSet<>();
			set.add("p.StandardCategorization");
			metadataInfo.setTaxonomyIds(set);
			Metadata metadata = new Metadata();
			metadataInfo.setMetadata(metadata);

			ContentWriteBuilder<OneImageBean> cwb = new ContentWriteBuilder<OneImageBean>();
			cwb.mainAspectData(damFiegWireImageAspectBean);
			cwb.type(OneImageBean.ASPECT_NAME);
			cwb.aspect("atex.Files", filesAspectBean);
			cwb.aspect("atex.Image", imageInfoAspectBean);
			cwb.aspect("p.InsertionInfo", insertionInfoAspectBean);
			cwb.aspect("atex.Metadata", metadataInfo);

			ContentWrite<OneImageBean> content = cwb.buildCreate();
			ContentResult<OneImageBean> cr = getContentManager().create(content, SYSTEM_SUBJECT);
			if(!cr.getStatus().isSuccess()){
				LOGGER.error("Error importing image: "+cleanPath+"." + cr.getStatus().toString());
			} else {
				imageContentId = cr.getContentId().getContentId();
				LOGGER.info("Inserted image with contentid: " + cr.getContentId().getContentId());
			}

			//	          // move image file
			//	          Path fileToMovePath = Paths.get(imageFilePath);
			//	          Path targetPath = directory.resolve("../backup");
			//	          Path fileTargetPath = targetPath.resolve(fileToMovePath.getFileName());
			//	          try {
			//	            Files.move(fileToMovePath, fileTargetPath,StandardCopyOption.REPLACE_EXISTING);
			//	          }
			//	          catch (Exception e) {
			//	            e.printStackTrace(); 
			//	          }

		} finally {
			IOUtils.closeQuietly(bis);
			bis = null;
		}
		return imageContentId;
	}

	public String cleanPath(final String path) {
		// return immediately if path is null
		if (path == null) {
			return null;
		}
		String cleanPath = DOUBLE_SLASH_PATTERN.matcher(path).replaceAll("/");
		if (cleanPath.startsWith("/")) {
			cleanPath = cleanPath.substring(1);
		}
		return cleanPath;
	}

	private static String getFormatName(Object o) {
		try {
			ImageInputStream iis = ImageIO.createImageInputStream(o);
			Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
			if (!iter.hasNext()) {
				return null;
			}
			ImageReader reader = (ImageReader) iter.next();
			iis.close();
			if (reader.getOriginatingProvider().getMIMETypes().length > 0)
				return reader.getOriginatingProvider().getMIMETypes()[0];
		} catch (IOException e) {
		}
		return null;
	}


}

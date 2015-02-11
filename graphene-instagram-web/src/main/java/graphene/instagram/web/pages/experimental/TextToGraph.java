package graphene.instagram.web.pages.experimental;

import graphene.model.idl.G_VisualType;
import graphene.web.annotations.PluginPage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

@PluginPage(visualType = G_VisualType.EXPERIMENTAL, menuName = "Text To Graph", icon = "fa fa-lg fa-fw fa-code-fork")
@Import(library = { "context:core/js/plugin/dropzone/dropzone.js",
		"context:/core/js/startdropzone.js" })
public class TextToGraph {

	@Property
	private UploadedFile file;
	@Inject
	private Logger logger;

	public void onSuccess() {

		BufferedReader br = new BufferedReader(new InputStreamReader(
				file.getStream()));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				logger.debug(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// file.write(copied);
	}
}

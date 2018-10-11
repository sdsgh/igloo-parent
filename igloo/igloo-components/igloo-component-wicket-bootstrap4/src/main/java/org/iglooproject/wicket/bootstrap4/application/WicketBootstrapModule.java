package org.iglooproject.wicket.bootstrap4.application;

import java.util.List;

import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.settings.ResourceSettings;
import org.iglooproject.sass.service.IScssService;
import org.iglooproject.wicket.bootstrap4.console.resources.CoreWicketConsoleResources;
import org.iglooproject.wicket.bootstrap4.console.template.style.CoreConsoleCssScope;
import org.iglooproject.wicket.bootstrap4.markup.html.template.css.bootstrap.CoreBootstrap4CssScope;
import org.iglooproject.wicket.bootstrap4.markup.html.template.css.fontawesome.CoreFontAwesome5CssScope;
import org.iglooproject.wicket.bootstrap4.markup.html.template.css.jqueryui.JQueryUiCssResourceReference;
import org.iglooproject.wicket.bootstrap4.markup.html.template.js.jqueryui.JQueryUIJavaScriptResourceReference;
import org.iglooproject.wicket.more.application.CoreWicketApplication;
import org.iglooproject.wicket.more.application.IWicketModule;
import org.iglooproject.wicket.more.markup.html.template.AbstractWebPageTemplate;
import org.iglooproject.wicket.request.mapper.StaticResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wicketstuff.wiquery.ui.themes.WiQueryCoreThemeResourceReference;

import com.google.common.collect.ImmutableList;

@Service
public class WicketBootstrapModule implements IWicketModule {

	@Autowired
	private IScssService scssService;

	@Override
	public void addResourceReplacements(CoreWicketApplication application) {
		application.addResourceReplacement(org.wicketstuff.wiquery.ui.JQueryUIJavaScriptResourceReference.get(), JQueryUIJavaScriptResourceReference.get());
		application.addResourceReplacement(WiQueryCoreThemeResourceReference.get(), JQueryUiCssResourceReference.get());
	}

	@Override
	public List<StaticResourceMapper> listStaticResources() {
		return ImmutableList.of(
				staticResourceMaper("/common", AbstractWebPageTemplate.class),
				staticResourceMaper("/font-awesome", CoreFontAwesome5CssScope.class)
		);
	}

	@Override
	public void updateResourceSettings(ResourceSettings resourceSettings) {
		resourceSettings.getStringResourceLoaders().addAll(
				0, // Override the keys in existing resource loaders with the following
				ImmutableList.of(
						new ClassStringResourceLoader(CoreWicketConsoleResources.class)
				)
		);
	}

	@Override
	public void registerImportScopes() {
		scssService.registerImportScope("core-bs4", CoreBootstrap4CssScope.class);
		scssService.registerImportScope("core-fa", CoreFontAwesome5CssScope.class);
		scssService.registerImportScope("core-console", CoreConsoleCssScope.class);
	}

}

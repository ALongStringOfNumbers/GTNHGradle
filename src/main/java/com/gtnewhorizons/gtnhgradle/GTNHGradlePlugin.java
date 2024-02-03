/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.gtnewhorizons.gtnhgradle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.gtnhgradle.modules.GitVersionModule;
import com.gtnewhorizons.retrofuturagradle.UserDevPlugin;
import de.undercouch.gradle.tasks.download.DownloadTaskPlugin;
import org.ajoberstar.grgit.gradle.GrgitPlugin;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.plugins.scala.ScalaPlugin;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.gradle.ext.IdeaExtPlugin;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * The main GTNH buildscript plugin object. You can access it in the buildscript as {@code gtnhGradle} and use it to
 * activate specific modules manually. The GTNH project will use the GTNHConventionPlugin that automatically activates
 * all modules.
 */
public class GTNHGradlePlugin implements Plugin<Project> {

    /**
     * Name of the project extension you can use to
     */
    public static final String PROJECT_EXT_NAME = "gtnhGradle";

    /**
     * Ran by Gradle when the plugin is applied to the project.
     * This applies the Java library and RetroFuturaGradle plugins, for the plugin to do anything else modules need to
     * be opted-in using other functions in this class.
     */
    public void apply(Project project) {
        final PluginManager plugins = project.getPluginManager();

        // Apply shared plugins used by all mods
        plugins.apply(JavaLibraryPlugin.class);
        plugins.apply(IdeaExtPlugin.class);
        plugins.apply(EclipsePlugin.class);
        plugins.apply(ScalaPlugin.class);
        plugins.apply(MavenPublishPlugin.class);
        plugins.apply(GrgitPlugin.class);
        plugins.apply(DownloadTaskPlugin.class);
        plugins.apply(UserDevPlugin.class); // RFG

        // Create the gtnhGradle extension as a Gradle DSL-extensible object
        final GTNHExtension extension = project.getObjects()
            .newInstance(GTNHExtension.class, project);
        project.getExtensions()
            .add(GTNHExtension.class, PROJECT_EXT_NAME, extension);
    }

    /**
     * The object registered as {@code project.gtnhGradle}
     */
    @SuppressWarnings("unused") // Forms the public API
    public static abstract class GTNHExtension implements ExtensionAware {

        /** Logging service used by the plugin */
        public final @NotNull Logger logger;

        /** Parsed properties associated with this project */
        public @NotNull PropertiesConfiguration configuration;

        /** A list of all available modules to activate */
        public static final List<Class<? extends GTNHModule>> ALL_MODULES = ImmutableList.of(GitVersionModule.class);
        /** A map of all available modules, indexed by their class name */
        public static final Map<String, Class<? extends GTNHModule>> MODULES_BY_NAME;

        static {
            final ImmutableMap.Builder<String, Class<? extends GTNHModule>> builder = ImmutableMap.builder();
            for (final Class<? extends GTNHModule> m : ALL_MODULES) {
                builder.put(m.getSimpleName(), m);
            }
            MODULES_BY_NAME = builder.build();
        }

        /**
         * For internal use only.
         *
         * @param project passed in to avoid having to pass the project object to every apply function
         */
        @Inject
        @ApiStatus.Internal
        public GTNHExtension(final Project project) {
            logger = Logging.getLogger(GTNHGradlePlugin.class);
            configuration = new PropertiesConfiguration(project);
        }

        /**
         * Activates a module by its class name, if enabled in properties (e.g. {@code GitVersionModule}).
         *
         * @param project The project to activate the module on
         * @param name    Class name of the module, without the package name
         * @throws IllegalArgumentException If the module was not found
         */
        public void applyModuleByName(final Project project, final String name) {
            final Class<? extends GTNHModule> klass = MODULES_BY_NAME.get(name);
            if (klass == null) {
                throw new IllegalArgumentException("Invalid module name " + name);
            }
            GTNHModule.applyIfEnabled(klass, this, project);
        }

        /**
         * Activates all available and enabled modules on the given project.
         *
         * @param project The project to activate the modules on
         */
        public void applyAllModules(final Project project) {
            for (final Class<? extends GTNHModule> moduleClass : ALL_MODULES) {
                GTNHModule.applyIfEnabled(moduleClass, this, project);
            }
        }

        /** @return Gradle-provided injected service */
        @Inject
        public abstract @NotNull ObjectFactory getObjectFactory();

        /** @return Gradle-provided injected service */
        @Inject
        public abstract @NotNull ProviderFactory getProviderFactory();

        /** @return Gradle-provided injected service */
        @Inject
        public abstract @NotNull ProjectLayout getProjectLayout();

        /** @return Gradle-provided injected service */
        @Inject
        public abstract @NotNull FileSystemOperations getFileSystemOperations();

        /** @return Gradle-provided injected service */
        @Inject
        public abstract @NotNull ArchiveOperations getArchiveOperations();

        /** @return Gradle-provided injected service */
        @Inject
        public abstract @NotNull ExecOperations getExecOperations();
    }
}

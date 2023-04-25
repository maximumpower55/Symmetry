import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.fernflower.FernflowerDecompiler;
import io.github.coolcrabs.brachyura.fabric.FabricContext;
import io.github.coolcrabs.brachyura.fabric.FabricLoader;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyCollector;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyFlag;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.quilt.QuiltMaven;
import io.github.coolcrabs.brachyura.quilt.SimpleQuiltProject;
import net.fabricmc.mappingio.tree.MappingTree;

public class Buildscript extends SimpleQuiltProject {
	private Versions versions = new Versions(getProjectDir().resolve("buildscript").resolve("versions.properties"));

	@Override
	public int getJavaVersion() {
		return Integer.parseInt(versions.JAVA.get());
	}

	@Override
	public @Nullable BrachyuraDecompiler decompiler() {
		return new FernflowerDecompiler(Maven.getMavenJarDep(QuiltMaven.URL, new MavenId("org.quiltmc", "quiltflower", versions.QUILTFLOWER.get())));
	}

	@Override
	public FabricLoader getLoader() {
		return new FabricLoader(QuiltMaven.URL, QuiltMaven.loader(versions.QUILT_LOADER.get()));
	}

	@Override
	public VersionMeta createMcVersion() {
		return Minecraft.getVersion(versions.MINECRAFT.get());
	}

	@Override
	public MappingTree createMappings() {
		return createMojmap();
	}

	@Override
	public void getModDependencies(ModDependencyCollector d) {
		jij(d.addMaven("https://jitpack.io/", new MavenId("com.github.LlamaLad7", "MixinExtras", versions.MIXIN_EXTRAS.get()), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));

		d.addMaven("https://maven.terraformersmc.com/", new MavenId("com.terraformersmc.terraform-api:terraform-dirt-api-v1:3.1.0"), ModDependencyFlag.COMPILE);
	}

	@Override
	protected FabricContext createContext() {
		return new SimpleQuiltContext() {
			/*
			When mixin extras loads after mixin,
			MixinObfuscationProcessorInjection.getSupportedAnnotationTypes() does not contain the additional mixin types.
			This method override fixes this.
			*/
			@Override
			public List<Path> getCompileDependencies() {
				LinkedList<Path> paths = new LinkedList<>();

				for (Path p : super.getCompileDependencies()) {
					if (p.getFileName().toString().contains("MixinExtras")) {
						paths.addFirst(p);
					} else {
						paths.addLast(p);
					}
				}

				return paths;
			}
		};
	}
}

package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.LinkedHashSet;
import java.util.List;

public class ChangelistUtil {

	private static final String vfsFileSepartor = "/";

	public static boolean isAbsolutePath(int pathType) {
		return pathType == 0;
	}

	public static boolean isRelativePathFromContentRoot(int pathType) {
		return pathType == 1;
	}

	public static boolean isRelativePathFromProjectRoot(int pathType) {
		return pathType == 2;
	}

	/**
	 * Creates a unique list of filenames from the given changelist files.
	 */
	public static LinkedHashSet<String> createFilenames(List<VirtualFile> changedFiles, Project project, int pathType) {

		String prjBaseDir = project.getBaseDir().getPath();

		final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

		LinkedHashSet<String> allFiles = new LinkedHashSet<String>(changedFiles.size());

		for (VirtualFile changeFile : changedFiles) {

			if (changeFile == null) {
				continue;
			}

			String path = changeFile.getPath();

			if (!isAbsolutePath(pathType)) {

				String relativePrefix;
				if (isRelativePathFromProjectRoot(pathType)) {
					relativePrefix = prjBaseDir;
				} else {
					VirtualFile contentRootForFile = fileIndex.getContentRootForFile(changeFile);
					relativePrefix = contentRootForFile.getPath();
				}

				if (changeFile.getPath().startsWith(relativePrefix)) {
					path = path.substring(relativePrefix.length());
				}

				if (path.startsWith(vfsFileSepartor)) {
					path = path.substring(vfsFileSepartor.length());
				}
			}

			if (allFiles.contains(path) == false) {
				allFiles.add(path);
			}
		}

		return allFiles;
	}

	public static String createFilenameFromChangelistName(String changelistName) {
		String name = changelistName.replace(" ", "_");
		name = name.replace(",", "");
		name = name.replace("/", "_");
		name = name.replace("\\", "_");

		// I'm always writing stuff like "PTR - 1234",
		// which turns in to "PTR_-_1234", blech
		name = name.replace("_-_", "-");

		return name;
	}
}

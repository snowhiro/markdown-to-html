package jp.dip.snowsaber;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class MarkdownParserMain {

	private static String destDir = new File(MarkdownParserMain.class.getResource("/").getFile())
								.getParentFile().getParentFile().getAbsolutePath()
								+ "/dest/";
			//"/Users/snowhiro/workspace/eclipse_workspace/markdown-to-html/dest/";

	public static void main(String[] args) throws Exception {

		MarkdownParserMain main = new MarkdownParserMain();
		String basePath = "";
		if (args.length > 0) {
			basePath = args[0];
		}
		List<File> fileList = readFile(new File(basePath));
		List<File> mdFileList = fileList.parallelStream().filter(file -> {
			return file.getName().endsWith(".md");
		}).collect(Collectors.toList());

		for (File mdFile : mdFileList) {
			String path = mdFile.getParentFile().getAbsolutePath().replace(basePath, "");
			main.execute(path, mdFile);
		}


	}

	private  void execute(String path, File mdFile) throws IOException {
		// Markdown のさまざまな記法に対応するためのオプションを設定
	    MutableDataSet options = new MutableDataSet();
	    options.set(Parser.EXTENSIONS,
	      Arrays.asList(
	        AnchorLinkExtension.create(), // 見出しにアンカーを付ける
	        StrikethroughExtension.create(), // 打ち消し線に対応
	        TablesExtension.create(), // テーブルに対応
	        TocExtension.create() // [TOC] の部分に目次を生成する
	      ));

	    // Markdown パーサーと HTML レンダラーを用意
	    Parser parser = Parser.builder(options).build();
	    HtmlRenderer renderer = HtmlRenderer.builder(options).build();

	    // Markdown ファイルを読み込む

		File inputFile = mdFile;//getClassPathFile(fileName);
	    String markdown = getTextBody(inputFile);

	    String templateHtml = getTextBody("template.html");

	    // Markdown を HTML に変換して出力
	    Node document = parser.parse(markdown);
	    String html = renderer.render(document);
	    String dirPath = destDir + path;
	    File dirFile = new File(dirPath);
	    if (!dirFile.exists()) {
	    	dirFile.mkdirs();
	    }
	    String filePath = dirPath + "/" + inputFile.getName().replace(".md", ".html");

	    String title = inputFile.getName().replace(".md", "");
	    String htmlBody = templateHtml.replace("{title}", title).replace("{body}", html);

	    exportHtml(filePath, htmlBody);
	}

	private String getTextBody(String inputPath) {
	    List<String> lines;
		try {
			lines = Files.readAllLines(getClassPathFile(inputPath).toPath(), StandardCharsets.UTF_8);
		    String textBody = String.join("\n", lines);
		    return textBody;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getTextBody(File inputFile) {
	    List<String> lines;
		try {
			lines = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);
		    String textBody = String.join("\n", lines);
		    return textBody;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File getClassPathFile(String fileName) {
		URL url = MarkdownParserMain.class.getClassLoader().getResource(fileName);
	    File inputFile = Paths.get(url.getPath()).toFile();
		return inputFile;
	}


	private void exportHtml(String filePath, String htmlBody) {
		Path path = Paths.get(filePath);
		try {
			Files.write(path, htmlBody.getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
     * 指定されたディレクトリ配下のファイルをすべて取得する。
     * @param targetFileList
     * @param file
     */
    public static List<File> readFile(File dir) {
    	List<File> fileList = new ArrayList<>();
    	readFile(fileList, dir);
    	return fileList;
    }

    /**
     * 指定されたディレクトリ配下のファイルをすべて取得する。
     * @param targetFileList
     * @param file
     */
    public static void readFile(List<File> targetFileList, File file) {
        if (file.isDirectory()) {
            List<File> fileList = Arrays.asList(file.listFiles());
            for (File targetFile : fileList) {
                readFile(targetFileList, targetFile);
            }
        } else {
            targetFileList.add(file);
        }
    }
}

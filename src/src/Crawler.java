import org.jetbrains.annotations.NotNull;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author  Adeola Adebayo
 * @version 1.0
 * @since   20-03-2018
 */

public class Crawler {

    private static HashMap<String, String> courseLinks = new HashMap<>();
    private static HashMap<String, String[]> downloadLinks = new HashMap<>();
    private static String sessionCookie = null;

    /**
     * This method parses through the html, searches for courses, searches for "resource" files
     * Adds
     * @throws IOException
     * @return null
     */
    private static void start() throws IOException {
        //Search for a specific course code
        Document searchResultsPage = Jsoup.connect(Const.MOODLE_BASE_ADDRESS + Const.SEARCH_ADDRESS)
                .data("search", "csc4")
                .cookie("MoodleSession", sessionCookie)
                .get();

        //Get all the link tags in the search results page
        Element content = searchResultsPage.getElementsByClass("courses course-search-result course-search-result-search").first();

        Elements links = content.getElementsByTag("a");

        //Loop through each link tag
        for (Element link : links) {
            //Check for any tags which match the search parameter
            if (link.text().contains("CSC424")) {
                //Add the relevant links to a list
                courseLinks.put(link.attr("href"), link.text());
            }

        }

        for (Map.Entry<String, String> courseLink : courseLinks.entrySet()) {
            Document document = Jsoup.connect(courseLink.getKey())
                    .cookie("MoodleSession", sessionCookie)
                    .get();

            int i = 0;
            Element e = document.getElementById("section-" + String.valueOf(i));
            while (!(e == null)) {

                for (Element a : e.getElementsByTag("a")) {
                    if (a.attr("href").contains("resource") && a.text().contains("File")) {
                        String[] arr = new String[2];

                        //Folder name
                        arr[0] = courseLink.getValue();

                        //File name
                        arr[1] = a.text();

                        downloadLinks.put(a.attr("href"), arr);
                    }
                }
                i++;
                e = document.getElementById("section-" + String.valueOf(i));
            }
        }


        for (Map.Entry<String, String[]> e : downloadLinks.entrySet()) {
            String[] arr;
            arr = e.getValue();
            Connection.Response response = getResponse(e.getKey());
            saveToFile(response.bodyStream(), arr[1], response.contentType(), arr[0]);
        }

    }

    /**
     * This method is used to create the directory to which files are downloaded
     * according to their respective courses
     * @param dirName Name of directory to be created (course name)
     * @return this returns the directory as a path (baseUserDir/dirName/)
     */
    private static String createDirectory(@NotNull String dirName) {

        String dir = System.getProperty("user.dir");
        dirName = dirName.replaceAll(":", "-");
        File file = new File(dir + File.separator + dirName);
        if (!file.exists()) {
            if (file.mkdirs()) {
                System.out.println(dirName + "  ++++  [HAS BEEN CREATED SUCCESSFULLY!]");
            } else {
                System.out.println("[FAILED TO CREATE DIRECTORY]  ++++  " + dirName);
                return null;
            }
        }
        return dir + File.separator + dirName;
    }

    /**
     * This method is used to obtain the response object from the server
     * @param URL url to obtain response from
     * @return this returns a jsoup Connection Response object
     */
    private static Connection.Response getResponse(@NotNull String URL) {
        Connection.Response response = null;
        try {
            response = Jsoup.connect(URL)
                    .cookie("MoodleSession", sessionCookie)
                    .ignoreContentType(true)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * This method determines the correct
     * file name and extension
     *
     * @param MIME MIME type gotten from response
     * @param folderPath Path to file directory
     * @param fileName file name
     * @return returns filePath/fileName.MIME_TYPE
     */
    private static String getFullName(@NotNull String MIME, @NotNull String folderPath, @NotNull String fileName) {

        String file = null;
        if (MIME.equals(Const.PPTX)) {
            file = folderPath + File.separator + fileName + ".pptx";
        }

        if (MIME.equals(Const.PPT)) {
            file = folderPath + File.separator + fileName + ".ppt";
        }

        if (MIME.equals(Const.DOC)) {
            file = folderPath + File.separator + fileName + ".doc";
        }

        if (MIME.equals(Const.DOCX)) {
            file = folderPath + File.separator + fileName + ".docx";
        }

        if (MIME.equals(Const.PDF)) {
            file = folderPath + File.separator + fileName + ".pdf";
        }

        return file;
    }


    /**
     * Saves the input stream to a folder/file.MIME based
     * on the input parameters
     *
     * @param inputStream file input stream
     * @param fileName file name
     * @param MIME MIME type - file extension
     * @param folderName folder name - where file will be created
     * @return null
     */
    private static void saveToFile(@NotNull BufferedInputStream inputStream, @NotNull String fileName, @NotNull String MIME, @NotNull String folderName) {
        try {
            fileName = fileName.replaceAll("File", "");
            fileName = fileName.replaceAll("/", " - ");
            fileName = fileName.replaceAll("'\'", " - ");

            String folderPath = createDirectory(folderName);
            if (folderPath == null) {
                System.exit(0);
            }

            BufferedOutputStream bufferedOutputStream;
            int data;

            File fullName = new File(getFullName(MIME, folderPath, fileName));

            if (!fullName.exists()) {
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(getFullName(MIME, folderPath, fileName)));
                while ((data = inputStream.read()) != -1) {
                    bufferedOutputStream.write(data);
                }
                System.out.println(fileName + "  ++++  [DOWNLOADED SUCCESSFULLY]");

                inputStream.close();
                bufferedOutputStream.close();
            }

            else
            System.out.println(fileName + "  ++++  [ALREADY EXISTS]");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        try {
            sessionCookie = Login.getCookie();
            start();
        } catch (Exception e) {
            System.out.println("[AN ERROR OCCURRED]   ++++  " + e.toString());
            e.printStackTrace();
        }
    }
}

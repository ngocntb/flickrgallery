package com.highfly.flickrgallery.network;

import android.net.Uri;
import android.util.Log;

import com.highfly.flickrgallery.entity.Author;
import com.highfly.flickrgallery.entity.GalleryItem;
import com.highfly.flickrgallery.entity.PhotoDetails;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: Fetch data from Flickr
 */
public class FlickrFetchr {
    enum Type {GET_RECENT, SEARCH, USER_INFO, PHOTO_INFO}

    public static final String TAG = "FlickrFetchr";
    public static final String PREF_SEARCH_QUERY = "searchQuery";
    public static final String PREF_LAST_RESULT_ID = "lastResultId";

    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "133190bbf383438d765aa9bfe28350cd";

    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";
    private static final String METHOD_USER_INFO = "flickr.people.getInfo";
    private static final String METHOD_PHOTO_INFO = "flickr.photos.getInfo";

    private static final String PARAM_EXTRAS = "extras";
    private static final String EXTRA_SMALL_URL = "url_s";

    private static final String XML_PHOTO = "photo";
    private static final String XML_RESULTS = "photos";
    private static final String XML_PERSON = "person";
    private static final String XML_TITLE = "title";
    private static final String XML_DESCRIPTION = "description";
    private static final String XML_USERNAME = "username";
    private static final String XML_TOTAL_RESULTS = "total";

    private String mTotalResults;

    private Type type;
    private HashMap<String, String> queryParams;

    private String getMethod() {
        switch (type) {
            case GET_RECENT:
                return METHOD_GET_RECENT;
            case SEARCH:
                return METHOD_SEARCH;
            case USER_INFO:
                return METHOD_USER_INFO;
            case PHOTO_INFO:
                return METHOD_PHOTO_INFO;
            default:
                return "";
        }
    }

    public String getTotalResult() {
        return mTotalResults;
    }

    /*
     * Send HTTP request, and get Response
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlSpec);
            connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            return out.toByteArray();
        } catch (Exception ex) {
            Log.e(TAG, urlSpec + ": " + ex.getMessage());
            return null;

        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }


    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }


    private String buildUrl(){
        Uri.Builder builder = Uri.parse(ENDPOINT).buildUpon();
        for(Map.Entry<String, String> entry : queryParams.entrySet()){
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build().toString();
    }

    /*
     * After receive HTTP response, pass into XmlPullParserFactory to parse the data
     */
    private XmlPullParser downloadData(String url)
            throws IOException, XmlPullParserException{
        String xmlString = getUrl(url);
        Log.i(TAG, "Received xml: " + xmlString);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(xmlString));
        return parser;
    }

    /*
     * Parse xml into entity
     * Parse GalleryItem
     */
    private void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser)
            throws XmlPullParserException, IOException{
        mTotalResults = null;
        int evenType = parser.next();
        while(evenType != XmlPullParser.END_DOCUMENT && mTotalResults == null){
            if(evenType == XmlPullParser.START_TAG && XML_RESULTS.equals(parser.getName())){
                mTotalResults = parser.getAttributeValue(null, XML_TOTAL_RESULTS);
            }
            evenType = parser.next();
        }

        while(evenType != XmlPullParser.END_DOCUMENT){
            if(evenType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())){
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);
                String author = parser.getAttributeValue(null, "owner");
                GalleryItem item = new GalleryItem(id, caption, smallUrl, author);
                items.add(item);
            }
            evenType = parser.next();
        }
    }

    /*
     * Parse xml into entity
     * Parse Author
     */
    private void parseAuthor(Author author, XmlPullParser parser)
            throws XmlPullParserException, IOException{
        int evenType = parser.next();
        boolean flag = true;

        while(evenType != XmlPullParser.END_DOCUMENT && flag){
            if(evenType == XmlPullParser.START_TAG && XML_PERSON.equals(parser.getName())){
                String iconFarm = parser.getAttributeValue(null, "iconfarm");
                String iconServer = parser.getAttributeValue(null, "iconserver");

                author.setIconFarm(iconFarm == null? 0 : Integer.parseInt(iconFarm));
                author.setIconServer(iconServer == null? 0 : Integer.parseInt(iconServer));
            }
            if(evenType == XmlPullParser.START_TAG && XML_USERNAME.equals(parser.getName())){
                evenType = parser.next();
                if(evenType == XmlPullParser.TEXT) {
                    String username = parser.getText();
                    author.setUsername(username);
                    flag = false;
                }
            }
            evenType = parser.next();
        }

    }

    /*
     * Parse xml into entity
     * Parse PhotoDetails
     */
    private void parsePhotoDetails(PhotoDetails details, XmlPullParser parser)
            throws XmlPullParserException, IOException{
        int evenType = parser.next();
        boolean flag = true;

        while(evenType != XmlPullParser.END_DOCUMENT && flag){
            if(evenType == XmlPullParser.START_TAG && XML_TITLE.equals(parser.getName())){
                evenType = parser.next();
                if(evenType == XmlPullParser.TEXT) {
                    String title = parser.getText();
                    details.setTitle(title);
                }
            }
            if(evenType == XmlPullParser.START_TAG && XML_DESCRIPTION.equals(parser.getName())){
                evenType = parser.next();
                if(evenType == XmlPullParser.TEXT) {
                    String description = parser.getText();
                    details.setDescription(description);
                    flag = false;
                }
            }
            evenType = parser.next();
        }

    }

    /*
     * Fetch Recent Photos from Flickr api
     * parameters: Method, api_key, extras, page
     * returns: xml file of Items
     */
    public ArrayList<GalleryItem> FetchItems(Integer page){
        type = Type.GET_RECENT;

        queryParams = new HashMap<>();
        queryParams.put("method", getMethod());
        queryParams.put("api_key", API_KEY);
        queryParams.put(PARAM_EXTRAS, EXTRA_SMALL_URL);
        queryParams.put("page", page.toString());

        String url = buildUrl();
        ArrayList<GalleryItem> items = new ArrayList<>();
        try {
            parseItems(items, downloadData(url));
        }catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items: ", ioe);
        } catch (XmlPullParserException xppe){
            Log.e(TAG, "Failed to parse items: ", xppe);
        }

        return items;
    }

    /*
    * Fetch Search Photos from Flickr api
    * parameters: Method, api_key, extras, search text, page
    * returns: xml file of Items
    */
    public ArrayList<GalleryItem> SearchItems(String searchText, Integer page){
        type = Type.SEARCH;

        queryParams = new HashMap<>();
        queryParams.put("method", getMethod());
        queryParams.put("api_key", API_KEY);
        queryParams.put(PARAM_EXTRAS, EXTRA_SMALL_URL);
        queryParams.put("text", searchText);
        queryParams.put("page", page.toString());

        String url = buildUrl();
        ArrayList<GalleryItem> items = new ArrayList<>();
        try {
            parseItems(items, downloadData(url));
        }catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items: ", ioe);
        } catch (XmlPullParserException xppe){
            Log.e(TAG, "Failed to parse items: ", xppe);
        }

        return items;
    }

    /*
    * Fetch Author from Flickr api
    * parameters: Method, api_key, user_id
    * returns: xml file of author information
    */
    public Author FetchAuthor(String userId){
        type = Type.USER_INFO;

        queryParams = new HashMap<>();
        queryParams.put("method", getMethod());
        queryParams.put("api_key", API_KEY);
        queryParams.put("user_id", userId);

        String url = buildUrl();
        Author author = new Author(userId);
        try {
            parseAuthor(author, downloadData(url));
        }catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items: ", ioe);
        } catch (XmlPullParserException xppe){
            Log.e(TAG, "Failed to parse items: ", xppe);
        }

        return author;
    }

    /*
    * Fetch Photo Details from Flickr api
    * parameters: Method, api_key, photo_id
    * returns: xml file of Photo Details
    */
    public PhotoDetails FetchPhotoDetails(String photoId){
        type = Type.PHOTO_INFO;

        queryParams = new HashMap<>();
        queryParams.put("method", getMethod());
        queryParams.put("api_key", API_KEY);
        queryParams.put("photo_id", photoId);

        String url = buildUrl();
        PhotoDetails photoDetails = new PhotoDetails();
        try {
            parsePhotoDetails(photoDetails, downloadData(url));
        }catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items: ", ioe);
        } catch (XmlPullParserException xppe){
            Log.e(TAG, "Failed to parse items: ", xppe);
        }

        return photoDetails;
    }

}

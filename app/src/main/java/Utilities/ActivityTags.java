package Utilities;

/**
 * Created by ivanj on 27/10/2016.
 */

public class ActivityTags {

    private static final String Main = "Main";
    private static final String ColorBlobDetection = "ColorBlobDetection";
    private static final String Menu ="Menu";
    private static final String OpenCvDefault = "OpenCvDefault";

    public String getMain() {
        return this.Main;
    }
    public String getColorBlobDetection() {
        return this.ColorBlobDetection;
    }
    public String getMenu() {
        return this.Menu;
    }
    public String getOpenCvDefault() {
        return this.OpenCvDefault;
    }

    private static final ActivityTags _activityTags = new ActivityTags();
    public static ActivityTags getActivity() {
        return _activityTags;
    }
}

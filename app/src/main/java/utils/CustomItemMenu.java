package utils;

import android.support.annotation.NonNull;

/**
 * Created by enriq on 17/3/2018.
 */

public class CustomItemMenu implements Comparable<CustomItemMenu>{
    private int order;
    private String name;
    private String url;
    private String activity;

    public CustomItemMenu(int order, String name, String url, String activity) {
        this.order = order;
        this.name = name;
        this.url = url;
        this.activity = activity;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    @Override
    public int compareTo(@NonNull CustomItemMenu customItemMenu) {
        if(this.order > customItemMenu.getOrder()){
            return 1;
        }else if(this.order < customItemMenu.getOrder()){

        }else{
            return 0;
        }
        return 0;
    }
}

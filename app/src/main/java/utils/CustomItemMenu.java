package utils;

import android.support.annotation.NonNull;

/**
 * Created by enriq on 17/3/2018.
 */

public class CustomItemMenu implements Comparable<CustomItemMenu>{
    private int order;
    private String name;
    private String url;
    private String fragment;

    public CustomItemMenu(int order, String name, String url, String fragment) {
        this.order = order;
        this.name = name;
        this.url = url;
        this.fragment = fragment;
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

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
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

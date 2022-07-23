package ai.deepar.deepar_example;

public class FeedPost {

   public String url;
   public String caption;
   public FilterUser user;

    public String getUrl() {
        return url;
    }

    public FilterUser getUser() {
        return user;
    }

    public void setUser(FilterUser user) {
        this.user = user;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    FeedPost(){

   }

    FeedPost(String url,String caption){
        this.url = url;
        this.caption = caption;
        this.url = url;
    }

}

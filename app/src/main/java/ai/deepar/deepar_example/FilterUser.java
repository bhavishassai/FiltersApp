package ai.deepar.deepar_example;

public class FilterUser {

    private String userName;
    private String profileUrl;

    public FilterUser(String userName, String profileUrl) {
        this.userName = userName;
        this.profileUrl = profileUrl;
    }

    FilterUser(){}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}

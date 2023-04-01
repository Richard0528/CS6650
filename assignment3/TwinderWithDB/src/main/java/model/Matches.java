package model;

import java.util.List;

public class Matches {

    private List<String> matchList = null;

    public Matches(List<String> matchList) {
        this.matchList = matchList;
    }

    public List<String> getMatchList() {
        return matchList;
    }

    public void setMatchList(List<String> matchList) {
        this.matchList = matchList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matches {\n");

        sb.append("    matchList: ").append(toIndentedString(matchList)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

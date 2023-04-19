package model;

public class MatchStats {

    private Integer numLlikes = null;

    private Integer numDislikes = null;

    public MatchStats(Integer numLlikes, Integer numDislikes) {
        this.numLlikes = numLlikes;
        this.numDislikes = numDislikes;
    }

    public Integer getNumLlikes() {
        return numLlikes;
    }

    public void setNumLlikes(Integer numLlikes) {
        this.numLlikes = numLlikes;
    }

    public Integer getNumDislikes() {
        return numDislikes;
    }

    public void setNumDislikes(Integer numDislikes) {
        this.numDislikes = numDislikes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MatchStats {\n");

        sb.append("    numLlikes: ").append(toIndentedString(numLlikes)).append("\n");
        sb.append("    numDislikes: ").append(toIndentedString(numDislikes)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

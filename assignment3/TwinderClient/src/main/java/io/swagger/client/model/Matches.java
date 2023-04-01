/*
 * twinder
 * CS6650 assignment API
 *
 * OpenAPI spec version: 1.2
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * Matches
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2023-03-30T04:13:03.899395166Z[GMT]")
public class Matches {
  @SerializedName("matchList")
  private List<String> matchList = null;

  public Matches matchList(List<String> matchList) {
    this.matchList = matchList;
    return this;
  }

  public Matches addMatchListItem(String matchListItem) {
    if (this.matchList == null) {
      this.matchList = new ArrayList<String>();
    }
    this.matchList.add(matchListItem);
    return this;
  }

   /**
   * Get matchList
   * @return matchList
  **/
  @Schema(description = "")
  public List<String> getMatchList() {
    return matchList;
  }

  public void setMatchList(List<String> matchList) {
    this.matchList = matchList;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Matches matches = (Matches) o;
    return Objects.equals(this.matchList, matches.matchList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(matchList);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Matches {\n");
    
    sb.append("    matchList: ").append(toIndentedString(matchList)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
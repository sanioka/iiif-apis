package de.digitalcollections.iiif.model.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import de.digitalcollections.iiif.model.image.ImageApiProfile.Format;
import de.digitalcollections.iiif.model.image.ImageApiProfile.Quality;
import de.digitalcollections.iiif.model.interfaces.Selector;
import java.awt.Dimension;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dmfs.rfc3986.encoding.Encoded;
import org.dmfs.rfc3986.encoding.Precoded;

/**
 * A selector that describes a region on an IIIF Image API resource.
 *
 * See http://iiif.io/api/annex/openannotation/#iiif-image-api-selector
 */
@JsonTypeName(ImageApiSelector.TYPE)
public class ImageApiSelector implements Selector {
  public static String CONTEXT = "http://iiif.io/api/annex/openannotation/context.json";
  public static final String TYPE = "iiif:ImageApiSelector";

  private static final Pattern REQUEST_PAT = Pattern.compile(
      "/?(?<identifier>[^/]+)" +
      "/(?<region>[^/]+)"  +
      "/(?<size>[^/]+)" +
      "/(?<rotation>[^/]+)" +
      "/(?<quality>[^/]+?)\\.(?<format>[^/]+?)$");

  private String identifier;
  private RegionRequest region;
  private SizeRequest size;
  private RotationRequest rotation;
  private Quality quality;
  private Format format;

  @JsonProperty("@context")
  public String getContext() {
    return CONTEXT;
  }

  @JsonProperty("@type")
  public String getType() {
    return TYPE;
  }

  public static ImageApiSelector fromImageApiUri(URI imageApiUri) {
    return fromString(imageApiUri.getPath());
  }

  public static ImageApiSelector fromString(String str) {
    Matcher matcher = REQUEST_PAT.matcher(str);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Malformed IIIF Image API request: " + str);
    }
    ImageApiSelector selector = new ImageApiSelector();
    selector.setIdentifier(new Precoded(matcher.group("identifier")).decoded().toString());
    selector.setRegion(matcher.group("region"));
    selector.setSize(matcher.group("size"));
    selector.setRotation(matcher.group("rotation"));
    selector.setQuality(Quality.valueOf(matcher.group("quality").toUpperCase()));
    selector.setFormat(Format.valueOf(matcher.group("format").toUpperCase()));
    return selector;
  }

  public URI asImageApiUri(URI baseUri) {
    String baseUriString = baseUri.toString();
    if (!baseUriString.endsWith("/")) {
      baseUriString = baseUriString + "/";
    }
    return URI.create(baseUriString + this.toString());
  }

  @Override
  public String toString() {
    return String.format(
        "%s%s/%s/%s/%s.%s",
        identifier != null ? urlEncode(identifier) + "/" : "",
        Objects.toString(region, "full"),
        Objects.toString(size, "full"),
        Objects.toString(rotation, "0"),
        Objects.toString(quality, "default"),
        Objects.toString(format, "jpg"));
  }

  /** The spec says we have to urlencode values, but only characters outside of the US ASCII range and gen-delims
   * from RFC3986. However, our URL library can only encode the full set of gen-delims (EXCEPT the colon) and sub-delims,
   * which iswhy we have to manually decode the encoded sub-delims...
   * Great and pragmatic choice for readability, more code for us :-) */
  private static String urlEncode(String str) {
    Set<String> excluded = ImmutableSet.of(
        ":","!", "$", "&", "'", "(", ")", "*", "+", ",", ";", "=");
    String encoded = new Encoded(str).toString();
    for (String ex : excluded) {
      encoded = encoded.replaceAll(new Encoded(ex).toString(), ex);
    }
    return encoded;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public RegionRequest getRegion() {
    return region;
  }

  public void setRegion(RegionRequest region) {
    this.region = region;
  }

  public void setRegion(String region) {
    this.region = RegionRequest.fromString(region);
  }

  public SizeRequest getSize() {
    return size;
  }

  public void setSize(SizeRequest size) {
    this.size = size;
  }

  public void setSize(String size) {
    this.size = SizeRequest.fromString(size);
  }

  public RotationRequest getRotation() {
    return rotation;
  }

  public void setRotation(RotationRequest rotation) {
    this.rotation = rotation;
  }

  public void setRotation(String rotation) {
    this.rotation = RotationRequest.fromString(rotation);
  }

  public Quality getQuality() {
    return quality;
  }

  public void setQuality(Quality quality) {
    this.quality = quality;
  }

  public Format getFormat() {
    return format;
  }

  public void setFormat(Format format) {
    this.format = format;
  }
}

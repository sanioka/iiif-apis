package de.digitalcollections.iiif.presentation.model.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.impl.IndexedListSerializer;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;

/**
 * This is a custom serializer for List<Object> that has some special cases required by
 * the IIIF specification, namely that "sequences", "images" and "otherContent" must
 * always be encoded as arrays, even if they only contain a single element.
 *
 * Apart from this, the code is identical to {@link IndexedListSerializer} and delegates
 * to it where possible.
 */
public final class IiifIndexedListSerializer
    extends AsArraySerializerBase<List<?>> {
  private final IndexedListSerializer defaultSerializer;
  private static final ImmutableSet<String> NEVER_UNWRAP = ImmutableSet.of(
      "sequences", "images", "otherContent");

  public IiifIndexedListSerializer(IndexedListSerializer defaultSerializer, TypeFactory tf) {
    super(List.class, tf.constructSimpleType(Object.class, new JavaType[]{}), false, null, null);
    this.defaultSerializer = defaultSerializer;
  }

  private IiifIndexedListSerializer(IiifIndexedListSerializer src, BeanProperty prop,
                                    TypeSerializer vts, JsonSerializer<?> valueSerializer,
                                    Boolean unwrapSingle) {
    super(src, prop, vts, valueSerializer, unwrapSingle);
    this.defaultSerializer = src.defaultSerializer;
  }



  @Override
  public AsArraySerializerBase<List<?>> withResolved(BeanProperty property, TypeSerializer vts,
      JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
    return new IiifIndexedListSerializer(this, property, vts, elementSerializer, unwrapSingle);
  }

  @Override
  public final void serialize(List<?> value, JsonGenerator gen, SerializerProvider provider)
      throws IOException
  {
    final int len = value.size();
    String currentName = gen.getOutputContext().getCurrentName();
    // Special case: Never unwrap certain fields
    if (!NEVER_UNWRAP.contains(currentName) && len == 1) {
      if (((_unwrapSingle == null) &&
          provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED))
          || (_unwrapSingle == Boolean.TRUE)) {
        defaultSerializer.serializeContents(value, gen, provider);
        return;
      }
    }
    gen.writeStartArray(len);
    defaultSerializer.serializeContents(value, gen, provider);
    gen.writeEndArray();
  }

  @Override
  protected void serializeContents(List<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    defaultSerializer.serializeContents(value, gen, provider);

  }

  @Override
  public boolean hasSingleElement(List<?> value) {
    return defaultSerializer.hasSingleElement(value);
  }

  @Override
  protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
    return defaultSerializer._withValueTypeSerializer(vts);
  }
}

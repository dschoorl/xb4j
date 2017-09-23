/*
 * Copyright 2012 Red Star Development / Dave Schoorl Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package info.rsdev.xb4j.util.file;

import info.rsdev.xb4j.model.BindingModel;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.codec.binary.Base32InputStream;
import org.apache.commons.codec.binary.Base32OutputStream;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

/**
 * Implementation of {@link IXmlCodingFactory}, offering support for Base64 coding. If you use this class in your
 * {@link BindingModel}, then you must make sure that Apache Commons-Codec (at least version 1.5) is on your runtime
 * classpath.
 *
 * @author Dave Schoorl
 */
public class DefaultXmlCodingFactory extends AbstractXmlCodingFactory {

    public static final String BASE64_CODING = "base64";
    public static final String BASE32_CODING = "base32";

    public static final DefaultXmlCodingFactory INSTANCE = new DefaultXmlCodingFactory();

    private DefaultXmlCodingFactory() {
        super();
    }

    @Override
    protected void registerCodingTypes() {
        registerCoding(BASE64_CODING, Base64InputStream.class, Base64OutputStream.class);
        registerCoding(BASE32_CODING, Base32InputStream.class, Base32OutputStream.class);
    }

    @Override
    public InputStream getEncodingStream(File fromFile, String xmlDecodingType, Object... parameters) {
        return super.getEncodingStream(fromFile, xmlDecodingType, prependParameter(Boolean.TRUE, parameters));
    }

    @Override
    public InputStream getEncodingStream(InputStream in, String xmlEncodingType, Object... parameters) {
        return super.getEncodingStream(in, xmlEncodingType, prependParameter(Boolean.TRUE, parameters));
    }

    @Override
    public OutputStream getDecodingStream(File toFile, String xmlEncodingType, Object... parameters) {
        return super.getDecodingStream(toFile, xmlEncodingType, prependParameter(Boolean.FALSE, parameters));
    }

    @Override
    public OutputStream getDecodingStream(OutputStream out, String xmlDecodingType, Object... parameters) {
        return super.getDecodingStream(out, xmlDecodingType, prependParameter(Boolean.FALSE, parameters));
    }

    private Object[] prependParameter(Boolean doEncoding, Object[] parameters) {
        if ((parameters == null) || (parameters.length == 0)) {
            parameters = new Object[3];
            parameters[0] = doEncoding;
            parameters[1] = -1;     // indicate no separation in lines
            parameters[2] = null;   // line separator not necessary
        }
        return parameters;
    }

}

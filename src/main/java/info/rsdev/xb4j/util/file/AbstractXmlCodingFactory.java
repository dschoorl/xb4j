/* Copyright 2012 Red Star Development / Dave Schoorl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.xb4j.util.file;

import info.rsdev.xb4j.exceptions.Xb4jException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IXmlCodingFactory}, offering support for Base64 coding.
 *
 * @author Dave Schoorl
 */
public abstract class AbstractXmlCodingFactory implements IXmlCodingFactory {

    private static final Map<Class<?>, Class<?>> AUTO_BOXING_SUPPORT = new HashMap<>(8);

    static {
        AUTO_BOXING_SUPPORT.put(byte.class, Byte.class);
        AUTO_BOXING_SUPPORT.put(short.class, Short.class);
        AUTO_BOXING_SUPPORT.put(int.class, Integer.class);
        AUTO_BOXING_SUPPORT.put(long.class, Long.class);
        AUTO_BOXING_SUPPORT.put(double.class, Double.class);
        AUTO_BOXING_SUPPORT.put(float.class, Float.class);
        AUTO_BOXING_SUPPORT.put(char.class, Character.class);
        AUTO_BOXING_SUPPORT.put(boolean.class, Boolean.class);
    }

    private Map<String, StreamTypePair> supportedTypes = null;

    protected AbstractXmlCodingFactory() {
        supportedTypes = new HashMap<>();
        registerCodingTypes();
    }

    protected abstract void registerCodingTypes();

    protected void registerCoding(String codingName, Class<? extends InputStream> inType, Class<? extends OutputStream> outType) {
        supportedTypes.put(codingName, new StreamTypePair(inType, outType));
    }

    @Override
    public InputStream getEncodingStream(File fromFile, String xmlDecodingType, Object... parameters) {
        if (fromFile == null) {
            throw new NullPointerException("Input File cannot be null");
        }
        validateCodingType(xmlDecodingType);	//prevent opening streams to file when not necessary
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(fromFile));
            return getEncodingStream(in, xmlDecodingType, parameters);
        } catch (IOException e) {
            throw new Xb4jException(String.format("Could not create an InputStream for File %s", fromFile), e);
        }
    }

    @Override
    public InputStream getEncodingStream(InputStream in, String xmlDecodingType, Object... parameters) {
        if (in == null) {
            throw new NullPointerException("InputStream cannot be null");
        }
        validateCodingType(xmlDecodingType);
        Class<? extends InputStream> decodingStreamType = supportedTypes.get(xmlDecodingType).getInputStreamType();
        if (decodingStreamType == null) {
            throw new Xb4jException(String.format("No decoding input stream registered for %s", xmlDecodingType));
        }
        return getInstance(decodingStreamType, in, parameters);
    }

    @Override
    public OutputStream getDecodingStream(File toFile, String xmlEncodingType, Object... parameters) {
        if (toFile == null) {
            throw new NullPointerException("Output File cannot be null");
        }
        validateCodingType(xmlEncodingType);	//prevent opening streams to file when not necessary
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(toFile));
            return getDecodingStream(out, xmlEncodingType, parameters);
        } catch (IOException e) {
            throw new Xb4jException(String.format("Could not create an OutputStream to File %s", toFile), e);
        }
    }

    @Override
    public OutputStream getDecodingStream(OutputStream out, String xmlEncodingType, Object... parameters) {
        if (out == null) {
            throw new NullPointerException("OutputStream cannot be null");
        }
        validateCodingType(xmlEncodingType);
        Class<? extends OutputStream> encodingStreamType = supportedTypes.get(xmlEncodingType).getOutputStreamType();
        if (encodingStreamType == null) {
            throw new Xb4jException(String.format("No encoding ouput stream registered for %s", xmlEncodingType));
        }
        return getInstance(encodingStreamType, out, parameters);
    }

    @Override
    public boolean supports(String xmlCodingType) {
        return supportedTypes.containsKey(xmlCodingType);
    }

    private void validateCodingType(String codingType) {
        if (!supports(codingType)) {
            throw new Xb4jException(String.format("Unsupported xml content coding type: %s", codingType));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstance(Class<T> type, Object stream, Object... parameters) {
        int newSize = (parameters == null ? 0 : parameters.length) + 1;
        Object[] callParameters = new Object[newSize];
        callParameters[0] = stream;
        if (parameters != null) {
            System.arraycopy(parameters, 0, callParameters, 1, parameters.length);
        }

        Constructor<?> targetConstructor = null;
        for (Constructor<?> candidate : type.getDeclaredConstructors()) {
            if (candidate.getParameterTypes().length == callParameters.length) {
                //compare each parameter if it's a compatible type
                boolean noneMatchingParameterFound = false;
                Class<?>[] formalParameters = candidate.getParameterTypes();
                for (int i = 0; i < formalParameters.length; i++) {
                    Class<?> formalParameter = formalParameters[i];
                    if (formalParameter.isPrimitive() && (!formalParameter.equals(void.class))) {
                        formalParameter = AUTO_BOXING_SUPPORT.get(formalParameter);
                    }

                    if ((callParameters[i] != null) && !formalParameter.isAssignableFrom(callParameters[i].getClass())) {
                        noneMatchingParameterFound = true;
                        break;
                    }
                }
                if (!noneMatchingParameterFound) {
                    targetConstructor = candidate;	//do not check if there are more constructors that match (for now)
                    break;
                }
            }
        }
        if (targetConstructor == null) {
            throw new Xb4jException("No constructor found");
        }

        if (!Modifier.isPublic(((Member) targetConstructor).getModifiers()) || !Modifier.isPublic(((Member) targetConstructor).getDeclaringClass().getModifiers())) {
            targetConstructor.setAccessible(true);
        }

        try {
            return (T) targetConstructor.newInstance(callParameters);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            throw new Xb4jException(String.format("Cannot create instance of %s with parameters %s", type, Arrays.asList(callParameters)), e);
        }
    }

    private static final class StreamTypePair {

        private final Class<? extends InputStream> inputStreamClass;
        private final Class<? extends OutputStream> outputStreamClass;

        public StreamTypePair(Class<? extends InputStream> inputStreamType, Class<? extends OutputStream> outputStreamType) {
            this.inputStreamClass = inputStreamType;
            this.outputStreamClass = outputStreamType;
        }

        public Class<? extends InputStream> getInputStreamType() {
            return this.inputStreamClass;
        }

        public Class<? extends OutputStream> getOutputStreamType() {
            return this.outputStreamClass;
        }
    }

}

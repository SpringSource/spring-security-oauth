/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.oauth2.provider.token.store.redis;

import org.springframework.core.serializer.support.SerializationFailedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serializes and objects using {@link ObjectOutputStream},
 * and deserializes only allowed objects using {@link SaferObjectInputStream}.
 *
 * @author efenderbosch
 * @author Artem Smotrakov
 */
public class JdkSerializationStrategy extends StandardStringSerializationStrategy {

    private static final byte[] EMPTY_ARRAY = new byte[0];

    /**
     * A list of classes which are allowed to deserialize.
     */
    private static final List<String> ALLOWED_CLASSES;

    static {
        List<String> classes = new ArrayList<String>();
        classes.add("java.lang.");
        classes.add("java.util.");
        classes.add("org.springframework.security");
        ALLOWED_CLASSES = Collections.unmodifiableList(classes);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T deserializeInternal(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            SaferObjectInputStream saferObjectInputStream = new SaferObjectInputStream(
                    new ByteArrayInputStream(bytes), ALLOWED_CLASSES);
            return (T) saferObjectInputStream.readObject();
        } catch (Exception e) {
            throw new SerializationFailedException("Failed to deserialize payload", e);
        }
    }

    @Override
    protected byte[] serializeInternal(Object object) {
        if (object == null) {
            return EMPTY_ARRAY;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializationFailedException("Failed to serialize object", e);
        }
    }

}

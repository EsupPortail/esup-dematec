/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package fr.univrouen.poste.utils;

import java.nio.charset.StandardCharsets;

/** Validation without trimming, case folding or truncating credentials. */
public final class CredentialValidation {
    private CredentialValidation() { }

    public static boolean validPassword(String value) {
        return value != null && value.codePoints().anyMatch(c -> !Character.isWhitespace(c) && !Character.isSpaceChar(c))
                && value.codePoints().noneMatch(c -> Character.isISOControl(c))
                && StandardCharsets.UTF_8.newEncoder().canEncode(value)
                && value.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public static boolean validToken(String value) {
        return value != null && value.codePoints().anyMatch(c -> !Character.isWhitespace(c) && !Character.isSpaceChar(c)) && value.length() <= 255
                && value.codePoints().noneMatch(c -> Character.isWhitespace(c)
                    || Character.isSpaceChar(c) || Character.isISOControl(c));
    }
}

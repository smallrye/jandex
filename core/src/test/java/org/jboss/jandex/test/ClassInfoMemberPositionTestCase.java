/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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
package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClassInfoMemberPositionTestCase {

    private Index index;

    @BeforeEach
    public void setUp() throws IOException {
        Indexer indexer = new Indexer();
        String prefix = "org/jboss/jandex/test/ClassInfoMemberPositionTestCase$";
        indexer.index(getClass().getClassLoader().getResourceAsStream(prefix + "TestEntity.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream(prefix + "MaxSizeTestEntity.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream(prefix + "OverMaxSizeTestEntity.class"));
        this.index = indexer.complete();
    }

    @Test
    public void testMembersUnsorted() {
        assertOriginalPositions(index);
    }

    @Test
    public void testMembersUnsortedAfterRoundtrip() throws IOException {
        assertOriginalPositions(IndexingUtil.roundtrip(index));
    }

    @Test
    public void testMaxMembersUnsortedAndSorted() {
        ClassInfo clazz = index.getClassByName(DotName.createSimple(MaxSizeTestEntity.class.getName()));
        assertNotNull(clazz);

        List<FieldInfo> unsortedFields = clazz.unsortedFields();
        for (int i = 0; i < 256; i++) {
            assertEquals(String.format("f%03d", 255 - i), unsortedFields.get(i).name());
        }

        List<FieldInfo> sortedFields = clazz.fields();
        for (int i = 0; i < 256; i++) {
            assertEquals(String.format("f%03d", i), sortedFields.get(i).name());
        }
    }

    @Test
    public void testOverMaxMembersUnsortedAndSorted() {
        ClassInfo clazz = index.getClassByName(DotName.createSimple(OverMaxSizeTestEntity.class.getName()));
        assertNotNull(clazz);

        List<FieldInfo> unsortedFields = clazz.unsortedFields();
        for (int i = 0; i < 257; i++) {
            assertEquals(String.format("f%03d", i), unsortedFields.get(i).name());
        }

        List<FieldInfo> sortedFields = clazz.fields();
        for (int i = 0; i < 257; i++) {
            assertEquals(String.format("f%03d", i), sortedFields.get(i).name());
        }
    }

    private static void assertOriginalPositions(Index index) {
        ClassInfo clazz = index.getClassByName(DotName.createSimple(TestEntity.class.getName()));
        assertNotNull(clazz);

        List<FieldInfo> fields = clazz.unsortedFields();
        int f = 0;
        assertEquals("z", fields.get(f++).name());
        assertEquals("omega", fields.get(f++).name());
        assertEquals("y", fields.get(f++).name());
        assertEquals("x", fields.get(f++).name());
        assertEquals("alpha", fields.get(f++).name());

        List<MethodInfo> methods = clazz.unsortedMethods();
        int m = 0;
        assertEquals("c", methods.get(m++).name());
        assertEquals("<init>", methods.get(m++).name());
        assertEquals("a", methods.get(m++).name());
        assertEquals("<init>", methods.get(m++).name());
        assertEquals("b", methods.get(m++).name());
        assertEquals("_", methods.get(m++).name());
    }

    static class TestEntity {
        String z;
        static int omega;

        void c() {
        }

        String y;
        String x;

        public TestEntity() {
        }

        void a() {
        }

        public TestEntity(String optional) {
        }

        void b() {
        }

        static long alpha;

        void _() {
        }
    }

    static class MaxSizeTestEntity {
        int f255;
        int f254;
        int f253;
        int f252;
        int f251;
        int f250;
        int f249;
        int f248;
        int f247;
        int f246;
        int f245;
        int f244;
        int f243;
        int f242;
        int f241;
        int f240;
        int f239;
        int f238;
        int f237;
        int f236;
        int f235;
        int f234;
        int f233;
        int f232;
        int f231;
        int f230;
        int f229;
        int f228;
        int f227;
        int f226;
        int f225;
        int f224;
        int f223;
        int f222;
        int f221;
        int f220;
        int f219;
        int f218;
        int f217;
        int f216;
        int f215;
        int f214;
        int f213;
        int f212;
        int f211;
        int f210;
        int f209;
        int f208;
        int f207;
        int f206;
        int f205;
        int f204;
        int f203;
        int f202;
        int f201;
        int f200;
        int f199;
        int f198;
        int f197;
        int f196;
        int f195;
        int f194;
        int f193;
        int f192;
        int f191;
        int f190;
        int f189;
        int f188;
        int f187;
        int f186;
        int f185;
        int f184;
        int f183;
        int f182;
        int f181;
        int f180;
        int f179;
        int f178;
        int f177;
        int f176;
        int f175;
        int f174;
        int f173;
        int f172;
        int f171;
        int f170;
        int f169;
        int f168;
        int f167;
        int f166;
        int f165;
        int f164;
        int f163;
        int f162;
        int f161;
        int f160;
        int f159;
        int f158;
        int f157;
        int f156;
        int f155;
        int f154;
        int f153;
        int f152;
        int f151;
        int f150;
        int f149;
        int f148;
        int f147;
        int f146;
        int f145;
        int f144;
        int f143;
        int f142;
        int f141;
        int f140;
        int f139;
        int f138;
        int f137;
        int f136;
        int f135;
        int f134;
        int f133;
        int f132;
        int f131;
        int f130;
        int f129;
        int f128;
        int f127;
        int f126;
        int f125;
        int f124;
        int f123;
        int f122;
        int f121;
        int f120;
        int f119;
        int f118;
        int f117;
        int f116;
        int f115;
        int f114;
        int f113;
        int f112;
        int f111;
        int f110;
        int f109;
        int f108;
        int f107;
        int f106;
        int f105;
        int f104;
        int f103;
        int f102;
        int f101;
        int f100;
        int f099;
        int f098;
        int f097;
        int f096;
        int f095;
        int f094;
        int f093;
        int f092;
        int f091;
        int f090;
        int f089;
        int f088;
        int f087;
        int f086;
        int f085;
        int f084;
        int f083;
        int f082;
        int f081;
        int f080;
        int f079;
        int f078;
        int f077;
        int f076;
        int f075;
        int f074;
        int f073;
        int f072;
        int f071;
        int f070;
        int f069;
        int f068;
        int f067;
        int f066;
        int f065;
        int f064;
        int f063;
        int f062;
        int f061;
        int f060;
        int f059;
        int f058;
        int f057;
        int f056;
        int f055;
        int f054;
        int f053;
        int f052;
        int f051;
        int f050;
        int f049;
        int f048;
        int f047;
        int f046;
        int f045;
        int f044;
        int f043;
        int f042;
        int f041;
        int f040;
        int f039;
        int f038;
        int f037;
        int f036;
        int f035;
        int f034;
        int f033;
        int f032;
        int f031;
        int f030;
        int f029;
        int f028;
        int f027;
        int f026;
        int f025;
        int f024;
        int f023;
        int f022;
        int f021;
        int f020;
        int f019;
        int f018;
        int f017;
        int f016;
        int f015;
        int f014;
        int f013;
        int f012;
        int f011;
        int f010;
        int f009;
        int f008;
        int f007;
        int f006;
        int f005;
        int f004;
        int f003;
        int f002;
        int f001;
        int f000;
    }

    static class OverMaxSizeTestEntity {
        int f256;
        int f255;
        int f254;
        int f253;
        int f252;
        int f251;
        int f250;
        int f249;
        int f248;
        int f247;
        int f246;
        int f245;
        int f244;
        int f243;
        int f242;
        int f241;
        int f240;
        int f239;
        int f238;
        int f237;
        int f236;
        int f235;
        int f234;
        int f233;
        int f232;
        int f231;
        int f230;
        int f229;
        int f228;
        int f227;
        int f226;
        int f225;
        int f224;
        int f223;
        int f222;
        int f221;
        int f220;
        int f219;
        int f218;
        int f217;
        int f216;
        int f215;
        int f214;
        int f213;
        int f212;
        int f211;
        int f210;
        int f209;
        int f208;
        int f207;
        int f206;
        int f205;
        int f204;
        int f203;
        int f202;
        int f201;
        int f200;
        int f199;
        int f198;
        int f197;
        int f196;
        int f195;
        int f194;
        int f193;
        int f192;
        int f191;
        int f190;
        int f189;
        int f188;
        int f187;
        int f186;
        int f185;
        int f184;
        int f183;
        int f182;
        int f181;
        int f180;
        int f179;
        int f178;
        int f177;
        int f176;
        int f175;
        int f174;
        int f173;
        int f172;
        int f171;
        int f170;
        int f169;
        int f168;
        int f167;
        int f166;
        int f165;
        int f164;
        int f163;
        int f162;
        int f161;
        int f160;
        int f159;
        int f158;
        int f157;
        int f156;
        int f155;
        int f154;
        int f153;
        int f152;
        int f151;
        int f150;
        int f149;
        int f148;
        int f147;
        int f146;
        int f145;
        int f144;
        int f143;
        int f142;
        int f141;
        int f140;
        int f139;
        int f138;
        int f137;
        int f136;
        int f135;
        int f134;
        int f133;
        int f132;
        int f131;
        int f130;
        int f129;
        int f128;
        int f127;
        int f126;
        int f125;
        int f124;
        int f123;
        int f122;
        int f121;
        int f120;
        int f119;
        int f118;
        int f117;
        int f116;
        int f115;
        int f114;
        int f113;
        int f112;
        int f111;
        int f110;
        int f109;
        int f108;
        int f107;
        int f106;
        int f105;
        int f104;
        int f103;
        int f102;
        int f101;
        int f100;
        int f099;
        int f098;
        int f097;
        int f096;
        int f095;
        int f094;
        int f093;
        int f092;
        int f091;
        int f090;
        int f089;
        int f088;
        int f087;
        int f086;
        int f085;
        int f084;
        int f083;
        int f082;
        int f081;
        int f080;
        int f079;
        int f078;
        int f077;
        int f076;
        int f075;
        int f074;
        int f073;
        int f072;
        int f071;
        int f070;
        int f069;
        int f068;
        int f067;
        int f066;
        int f065;
        int f064;
        int f063;
        int f062;
        int f061;
        int f060;
        int f059;
        int f058;
        int f057;
        int f056;
        int f055;
        int f054;
        int f053;
        int f052;
        int f051;
        int f050;
        int f049;
        int f048;
        int f047;
        int f046;
        int f045;
        int f044;
        int f043;
        int f042;
        int f041;
        int f040;
        int f039;
        int f038;
        int f037;
        int f036;
        int f035;
        int f034;
        int f033;
        int f032;
        int f031;
        int f030;
        int f029;
        int f028;
        int f027;
        int f026;
        int f025;
        int f024;
        int f023;
        int f022;
        int f021;
        int f020;
        int f019;
        int f018;
        int f017;
        int f016;
        int f015;
        int f014;
        int f013;
        int f012;
        int f011;
        int f010;
        int f009;
        int f008;
        int f007;
        int f006;
        int f005;
        int f004;
        int f003;
        int f002;
        int f001;
        int f000;
    }
}

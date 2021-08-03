/*
 * JBoss, Home of Professional Open Source. Copyright 2021 Red Hat, Inc., and
 * individual contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.jboss.jandex.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.ModuleInfo;
import org.jboss.jandex.ModuleInfo.ExportedPackageInfo;
import org.jboss.jandex.ModuleInfo.OpenedPackageInfo;
import org.jboss.jandex.ModuleInfo.ProvidedServiceInfo;
import org.jboss.jandex.ModuleInfo.RequiredModuleInfo;
import org.junit.Before;
import org.junit.Test;

public class ModuleInfoTestCase {

    ModuleInfo mod;

    @Before
    public void setup() throws IOException {
        mod = indexModuleInfo();
    }

    @Test
    public void testModuleAnnotations() throws IOException {
        ModuleInfo mod = indexModuleInfo();
        Map<DotName, List<AnnotationInstance>> annotations = mod.moduleInfoClass().annotations();
        assertEquals(2, annotations.size());
        assertEquals(1, annotations.get(DotName.createSimple(Deprecated.class.getName())).size());
        assertEquals(1, annotations.get(DotName.createSimple("test.ModuleAnnotation")).size());
    }

    @Test
    public void testModulePackagesListed() throws IOException {
        ModuleInfo mod = indexModuleInfo();
        List<DotName> expected = Arrays.asList(DotName.createSimple("test"),
                                               DotName.createSimple("test.exec"));
        assertEquals(expected.size(), mod.packages().size());
        for (DotName e : expected) {
            assertTrue(mod.packages().contains(e));
        }
    }

    @Test
    public void testModuleRequires() {
        List<RequiredModuleInfo> requires = mod.requires();
        assertEquals(2, requires.size());
        assertEquals("java.base", requires.get(0).name().toString());
        assertEquals("java.desktop", requires.get(1).name().toString());
        assertTrue(requires.get(1).isTransitive());
    }

    @Test
    public void testModuleExports() {
        List<ExportedPackageInfo> exports = mod.exports();
        assertEquals(1, exports.size());
        assertEquals("test", exports.get(0).source().toString());
        assertEquals("java.base", exports.get(0).targets().get(0).toString());
        assertEquals("java.desktop", exports.get(0).targets().get(1).toString());
    }

    @Test
    public void testModuleOpens() {
        List<OpenedPackageInfo> opens = mod.opens();
        assertEquals(2, opens.size());
        assertEquals("test", opens.get(0).source().toString());
        assertEquals("java.base", opens.get(0).targets().get(0).toString());
        assertEquals("test.exec", opens.get(1).source().toString());
        assertEquals("java.base", opens.get(1).targets().get(0).toString());
    }

    @Test
    public void testModuleProvides() {
        List<ProvidedServiceInfo> provides = mod.provides();
        assertEquals(1, provides.size());
        assertEquals("test.ServiceProviderExample", provides.get(0).service().toString());
        assertEquals("test.ServiceProviderExample$ServiceProviderExampleImpl",
                     provides.get(0).providers().get(0).toString());
    }

    @Test
    public void testModuleUses() {
        List<DotName> uses = mod.uses();
        assertEquals(1, uses.size());
        assertEquals("test.ServiceProviderExample", uses.get(0).toString());
    }

    @Test
    public void testModuleMainClass() {
        assertNotNull(mod.mainClass());
        assertEquals("test.exec.Main", mod.mainClass().toString());
    }

    private ModuleInfo indexModuleInfo() throws IOException {
        Index index = buildIndex();
        return index.getModuleByName(DotName.createSimple("org.jboss.jandex.typeannotationtest"));
    }

    private Index buildIndex() throws IOException {
        Indexer indexer = new Indexer();
        indexAvailableModuleInfo(indexer);

        Index index = indexer.complete();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new IndexWriter(baos).write(index);

        index = new IndexReader(new ByteArrayInputStream(baos.toByteArray())).read();

        return index;
    }

    /**
     * Index all module-info.class files on the classpath.
     *
     * @param indexer the Indexer where the module-info.class will be added
     * @throws IOException
     */
    static void indexAvailableModuleInfo(Indexer indexer) throws IOException {
        Enumeration<URL> modules = ModuleInfoTestCase.class.getClassLoader().getResources("module-info.class");

        while (modules.hasMoreElements()) {
            URL module = modules.nextElement();
            indexer.index(module.openStream());
        }
    }
}

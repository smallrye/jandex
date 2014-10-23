package org.jboss.jandex;

import java.io.IOException;

/**
 * @author Jason T. Greene
 */
abstract class IndexReaderImpl {
    abstract Index read(int version) throws IOException;
}

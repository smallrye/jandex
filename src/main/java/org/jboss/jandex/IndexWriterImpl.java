package org.jboss.jandex;

import java.io.IOException;

/**
 * @author Jason T. Greene
 */
abstract class IndexWriterImpl {
    abstract int write(Index index, int version) throws IOException;
}

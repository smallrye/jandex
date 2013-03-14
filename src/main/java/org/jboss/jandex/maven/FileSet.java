/*******************************************************************************
 * Copyright (C) 2013 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.jboss.jandex.maven;

import java.io.File;
import java.util.List;

public class FileSet
{

    private File directory;

    private List<String> includes;

    private List<String> excludes;

    private boolean useDefaultExcludes;

    public File getDirectory()
    {
        return directory;
    }

    public List<String> getIncludes()
    {
        return includes;
    }

    public List<String> getExcludes()
    {
        return excludes;
    }

    public void setDirectory( final File directory )
    {
        this.directory = directory;
    }

    public void setIncludes( final List<String> includes )
    {
        this.includes = includes;
    }

    public void setExcludes( final List<String> excludes )
    {
        this.excludes = excludes;
    }

    public boolean isUseDefaultExcludes()
    {
        return useDefaultExcludes;
    }

    public void setUseDefaultExcludes( final boolean useDefaultExcludes )
    {
        this.useDefaultExcludes = useDefaultExcludes;
    }

}

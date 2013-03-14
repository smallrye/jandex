Jandex Plugin for Apache Maven
==============================

Full documentation can be found at: [http://jdcasey.github.com/jandex-maven-plugin](http://jdcasey.github.com/jandex-maven-plugin)

Basic Usage
-----------

    <build>
      <plugins>
        <plugin>
          <groupId>org.jboss.jandex</groupId>
          <artifactId>jandex-maven-plugin</artifactId>
          <version>1.0.1</version>
          <executions>
            <execution>
              <id>make-index</id>
              <goals>
                <goal>jandex</goal>
              </goals>
              <!-- phase is 'process-classes by default' -->
              <configuration>
                <!-- Nothing needed here for simple cases -->
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

This configuration will index all `.class` files in your `target/classes` directory, and write the index to `target/classes/META-INF/jandex.idx`.

Advanced Usage
--------------

If you need to process more than one directory of classes, you can specify multiple `fileSets` like this:

    <build>
      <plugins>
        <plugin>
          <groupId>org.jboss.jandex</groupId>
          <artifactId>jandex-maven-plugin</artifactId>
          <version>1.0.1</version>
          <executions>
            <execution>
              <id>make-index</id>
              <goals>
                <goal>jandex</goal>
              </goals>
              <!-- phase is 'process-classes by default' -->
              <configuration>
                <fileSets>
                  <fileSet>
                    <directory>${project.build.directory}/generated-classes/foo</directory>
                  </fileSet>
                  <fileSet>
                    <directory>${project.build.directory}/generated-classes/bar</directory>
                  </fileSet>
                </fileSets>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

To turn *off* processing for `target/classes`, specify the following configuration:

    <processDefaultFileSet>false</processDefaultFileSet>

For any `fileSet`, you can also fine-tune the classes that are processed using the following options:

    <fileSet>
      <directory>${project.build.directory}/somedir</directory>
      <!-- included globs -->
      <includes>
        <include>**/indexed/*.class</include>
        <include>**/idxd/*.class</include>
      </includes>
      <!-- excluded globs -->
      <excludes>
        <exclude>**/*NotIndexed.class</exclude>
      </excludes>
      <!-- normally true, this excludes things like the CVS/ and .svn/ directories, log files, etc. -->
      <useDefaultExcludes>false</useDefaultExcludes>
    </fileSet>

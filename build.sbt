libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

// For ScalaMeter
resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.github.axel22" %% "scalameter" % "0.4"

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
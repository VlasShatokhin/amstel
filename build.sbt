lazy val amstel = (project in file("."))
  .configure(ProjectProfiles.rootProfile)
  .aggregate(api, state, `publisher`, `statistics-reader`, core)

lazy val api = project
  .configure(ProjectProfiles.apiProfile)
  .dependsOn(`publisher`, `statistics-reader`)

lazy val `publisher` = project
  .configure(ProjectProfiles.publisherProfile)
  .dependsOn(state)

lazy val `statistics-reader` = project
  .configure(ProjectProfiles.statisticsReaderProfile)
  .dependsOn(state)

lazy val state = project
  .configure(ProjectProfiles.stateProfile)
  .dependsOn(core)

lazy val core = project
  .configure(ProjectProfiles.coreProfile)

package org.sonarsource.plugins.mybatis.fillers;

import org.antlr.sql.models.AntlrContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

public interface Filler {
	void fill(InputFile file, SensorContext context, AntlrContext antlrContext, Integer startLine);
}

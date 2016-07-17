package org.unclazz.jp1ajs2.findunit;

import java.io.File;
import java.util.List;

public final class Parameters {
	private File sourceFile;
	private String unitNamePattern;
	private String paramName;
	private List<String> paramValuePatterns;
	private OutputFormat outputFormat;
	
	public File getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}
	public String getUnitNamePattern() {
		return unitNamePattern;
	}
	public void setUnitNamePattern(String unitNamePattern) {
		this.unitNamePattern = unitNamePattern;
	}
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramNamePattern) {
		this.paramName = paramNamePattern;
	}
	public List<String> getParamValuePatterns() {
		return paramValuePatterns;
	}
	public void setParamValuePatterns(List<String> paramValuePatterns) {
		this.paramValuePatterns = paramValuePatterns;
	}
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}
	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}
}

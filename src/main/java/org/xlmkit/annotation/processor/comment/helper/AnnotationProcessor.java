package org.xlmkit.annotation.processor.comment.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("com.xlmkit.comment.helper.GenerateCommentFile")
public class AnnotationProcessor extends AbstractProcessor {
	private ProcessingEnvironment env;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		this.env = env;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getRootElements()) {
			doElement(element);
		}
		return false;
	}

	private void doElement(Element element) {
		if (element.getAnnotation(GenerateCommentFile.class) == null) {
			return;
		}
		if (!TypeElement.class.isAssignableFrom(element.getClass())) {
			return;
		}
		StringBuffer classBuffer = new StringBuffer();
		TypeElement typeEl = (TypeElement) element;
		List<ExecutableElement> mEls = ElementFilter.methodsIn(typeEl.getEnclosedElements());
		String packageName = initPackageName(typeEl);
		String newFileSimpleName = element.getSimpleName().toString() + ".comment.properties";

		for (ExecutableElement mEl : mEls) {

			String comment = env.getElementUtils().getDocComment(mEl);
			comment = comment == null ? "" : comment;
			for (String item : comment.split("\n")) {
				classBuffer.append("##  ");
				classBuffer.append(item);
				classBuffer.append("\n");
			}
			String key = mEl.getSimpleName().toString() + "#";
			String psNames = "";
			for (VariableElement vEl : mEl.getParameters()) {
				key += "#" + vEl.asType().toString();
				psNames += vEl.toString() + ",";
			}
			classBuffer.append(key);
			classBuffer.append("=");
			String base64 = Base64.getEncoder().encodeToString(comment.getBytes(StandardCharsets.UTF_8));
			classBuffer.append(base64);
			classBuffer.append("\n");
			classBuffer.append("parameter.names#" + key + "=" + psNames + "\n");
		}

		try {
			String code = classBuffer.toString();
			createCommenFile(packageName, newFileSimpleName, typeEl, code);
		} catch (Exception e) {
			env.getMessager().printMessage(Kind.ERROR, e.getMessage(), typeEl);
		}

	}

	private void createCommenFile(String pkg, String relativeName, TypeElement element, String content)
			throws IOException {
		FileObject fileObject = env.getFiler().createResource(//
				StandardLocation.CLASS_OUTPUT, pkg, relativeName, element);
		OutputStream aaaa = fileObject.openOutputStream();
		aaaa.write(content.getBytes(StandardCharsets.UTF_8));
		aaaa.close();
	}

	private String initPackageName(TypeElement element) {
		String name = element.toString();
		String simpleName = element.getSimpleName().toString();
		return name.substring(0, name.length() - 1 - simpleName.length());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_8;
	}
}

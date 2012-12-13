import japa.parser.JavaParser;
import japa.parser.ast.Comment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.LineComment;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

public class JavaSourceData {

	/*
	 * List of method declarations in the source file
	 */
	private List<String> methodList;

	/*
	 * List of field declarations in the source file
	 */
	private List<String> fieldList;

	/*
	 * List of comments in the source file
	 */
	private List<String> lineCommentList;

	private CompilationUnit cu;

	public JavaSourceData() {

	}

	public void setFile(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		try {
			// parse the file
			cu = JavaParser.parse(in);

		} finally {
			in.close();
		}
		// visit and print the methods names
		MethodVisitor mv = new MethodVisitor();
		mv.clearMethodList();
		mv.visit(cu, null);
		methodList = mv.getMethodList();
		/*
		 * for(String s : methodList) { System.out.println(s); }
		 */

		// new FieldVisitor().visit(cu, null);
		FieldVisitor fv = new FieldVisitor();
		fv.clearFieldList();
		fv.visit(cu, null);
		fieldList = fv.getFieldList();
		/*
		 * for(String s : fieldList) { System.out.println(s); }
		 */

		lineCommentList = this.getCommentList();

	}

	public List<String> getMethodList() {
		return methodList;
	}

	public List<String> getFieldList() {
		return fieldList;
	}

	public List<String> getCommentList() {
		// TODO this seems buggy, it doesn't seem to get every comment line...
		List<Comment> comments = cu.getComments();
		lineCommentList = new LinkedList<String>();
		if (comments != null) {
			for (Comment comment : comments) {
				lineCommentList.add(comment.getContent());
			}
		}
		return lineCommentList;
	}

	public CompilationUnit getCompilationUnit() {
		return cu;
	}

	/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	private static class MethodVisitor extends VoidVisitorAdapter {

		private LinkedList<String> methodNames;

		@Override
		public void visit(MethodDeclaration n, Object arg) {
			// here you can access the attributes of the method.
			// this method will be called for all methods in this
			// CompilationUnit, including inner class methods

			methodNames.add(n.getName()); // TODO may become getName
			// System.out.println(n.toString());

		}

		public void clearMethodList() {
			methodNames = new LinkedList<String>();
		}

		public List<String> getMethodList() {
			return methodNames;
		}
	}

	/**
	 * Simple visitor implementation for visiting FieldDeclaration nodes.
	 * 
	 * @author Chris
	 * 
	 */
	private static class FieldVisitor extends VoidVisitorAdapter {

		private LinkedList<String> fieldList;

		@Override
		public void visit(FieldDeclaration n, Object arg) {
			// here you can access the attributes of the method.
			// this method will be called for all methods in this
			// CompilationUnit, including inner class methods

			fieldList.add(n.toString());
			// System.out.println(n.toString());

		}

		public void clearFieldList() {
			fieldList = new LinkedList<String>();
		}

		public List<String> getFieldList() {
			return fieldList;
		}
	}

}

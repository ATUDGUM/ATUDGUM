package get;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;

public class get {
	public static boolean isSameMethod(MethodDeclaration oldMethod, MethodDeclaration newMethod){
		if (getModifierFromDeclaration(oldMethod).equals(getModifierFromDeclaration(newMethod)) 
				&& getMethodNameWithParaFromDeclaration(oldMethod).equals(getMethodNameWithParaFromDeclaration(newMethod))) {
			return true;
		}
		return false;
	}
	
	public static boolean isSameMethod(ConstructorDeclaration oldMethod, ConstructorDeclaration newMethod){
		if (getModifierFromDeclaration(oldMethod).equals(getModifierFromDeclaration(newMethod)) 
				&& getMethodNameWithParaFromDeclaration(oldMethod).equals(getMethodNameWithParaFromDeclaration(newMethod))) {
			return true;
		}
		return false;
	}
	
//-------------------------------------------------------------------------------------------------------------------------------
	public static int countParameters(String methodNameWithPara) { // method name should have parameter types
		int numOfPara;
		int countDou = 0;
		int index = -1;
		boolean useful = true;
		for (int i = 0; i<methodNameWithPara.length(); i++) {
			if (methodNameWithPara.charAt(i) == '(') {
				index = i;
			}else if (methodNameWithPara.charAt(i) == '<') {
				useful = false;
			}else if (methodNameWithPara.charAt(i) == '>') {
				useful =true;
			}else if (methodNameWithPara.charAt(i) == ',' && useful) {
				countDou++;
			}
		}
		if(methodNameWithPara.charAt(index+1) == ')') {
			numOfPara = 0;
		}else {
			numOfPara = 1 + countDou;
		}
		return numOfPara;
	}

//-------------------------------------------------------------------------------------------------------------------------------
	public static String getModifierFromEntity(SourceCodeEntity e) {
		if (e.isPublic()){
			return "Public ";
		}
		else if (e.isProtected()) {
			return "Protected ";
		}
		else if (e.isPrivate()) {
			return "Private ";
		}
		else {
			return "";
		}
	}
	
	public static String getModifierFromEntity(StructureEntityVersion e) {
		if (e.isPublic()){
			return "Public ";
		}
		else if (e.isProtected()) {
			return "Protected ";
		}
		else if (e.isPrivate()) {
			return "Private ";
		}
		else {
			return "";
		}
	}
	
	public static String getModifierFromDeclaration(MethodDeclaration e) {
		if (e.isPublic()){
			return "Public ";
		}
		else if (e.isProtected()) {
			return "Protected ";
		}
		else if (e.isPrivate()) {
			return "Private ";
		}
		else {
			return "";
		}
	}
	
	public static String getModifierFromDeclaration(ConstructorDeclaration e) {
		if (e.isPublic()){
			return "Public ";
		}
		else if (e.isProtected()) {
			return "Protected ";
		}
		else if (e.isPrivate()) {
			return "Private ";
		}
		else {
			return "";
		}
	}
	
//-------------------------------------------------------------------------------------------------------------------------------
	public static String getObjectNameFromEntity(SourceCodeEntity e) {
		String str = e.getUniqueName();
		int left = -1;
		int right = -1;
		for (int i = str.length()-1; i>=0; i--) {
			if (str.charAt(i) == ' ') {
				right = i;
			}
			if (str.charAt(i) == '.'){
				left = i+1;
				break;
			}
		}
		if (left == -1 || right == -1) {
			return "";
		}
		return str.substring(left, right);
	}
	
	public static String getObjectTypeFromEntity(SourceCodeEntity e) {
		String str = e.getUniqueName();
		int idx = -1;
		for (int i = str.length()-1; i>=0; i--) {
			if (str.charAt(i) == ' '){
				idx = i+1;
				break;
			}
		}
		if (idx == -1){
			return "";
		}
		return str.substring(idx);
	}
	
//-------------------------------------------------------------------------------------------------------------------------------
	public static String getMethodNameFromEntity(SourceCodeEntity e) { 
		String ent = e.getUniqueName().toString();
		int left = -1;
		int right = -1;
		for (int i = ent.length() - 1; i >= 0; i--){
			if (ent.charAt(i) == ')') {
				right = i;
			}
			if (ent.charAt(i) == '('){
				left = i;
				break;
			}
		}
		if (left == -1 || right == -1) {
			return "";
		}
		for (int i = left - 1; i >= 0; i--){
			if (ent.charAt(i) == '.'){
				return ent.substring(i+1);
			}
		}
		return "";
	}
	
	public static String getMethodNameFromRootEntity(StructureEntityVersion e) { 
		String ent = e.toString();
		int left = -1;
		int right = -1;
		for (int i = ent.length() - 1; i >= 0; i--){
			if (ent.charAt(i) == ')') {
				right = i;
			}
			if (ent.charAt(i) == '('){
				left = i;
				break;
			}
		}
		if (left == -1 || right == -1) {
			return "";
		}
		for (int i = left - 1; i >= 0; i--){
			if (ent.charAt(i) == '.'){
				return ent.substring(i+1);
			}
		}
		return "";
	}
	
	public static String getMethodNameFromDeclaration(MethodDeclaration md) { 
		String str = md.getDeclarationAsString(false, false, false);
		char[] strc = str.toCharArray();
		int idx1 = 0;
		int idx2 = 0;
		boolean shouldPass = false;
		for (int i = 0; i < strc.length; i++){
			if (strc[i] == '<') {
				shouldPass = true;
			}
			if (strc[i] == '>'){
				shouldPass = false;
			}
			if (strc[i] == ' ' && !shouldPass) {
				idx1 = i + 1;
				break;
			}
		}
		for (int i = 0; i < strc.length; i++){
			if (strc[i] == '(') {
				idx2 = i;
				break;
			}
		}
		return str.substring(idx1, idx2);
	}
	
	public static String getMethodNameFromDeclaration(ConstructorDeclaration md) { 
		int idx = -1;
		int left = -1;
		for (int i = md.getDeclarationAsString(false, false, false).length() - 1; i >= 0; i--) {
			if(md.getDeclarationAsString(false, false, false).charAt(i) == '(') {
				left = i;
				break;
			}
		}
		for (int i = left; i >= 0; i--) {
			if (md.getDeclarationAsString(false, false, false).charAt(i) == ' ') {
				idx = i;
			}
		}
		return md.getDeclarationAsString(false, false, false).substring(idx+1, left);
	}
	
	public static String getMethodNameWithParaFromDeclaration(MethodDeclaration md) { 
		return getMethodNameFromDeclaration(md) + getParameterListAsString(md);
	}
	
	public static String getMethodNameWithParaFromDeclaration(ConstructorDeclaration md) { 
		return getMethodNameFromDeclaration(md) + getParameterListAsString(md);
	}
	
//-------------------------------------------------------------------------------------------------------------------------------	
	public static String getParameterListAsString(MethodDeclaration md) {
		String ans = "(";
		List<Type> parameterTypes = new ArrayList<>();
		parameterTypes = extractMethodParameterTypes(md);
		for (Type t: parameterTypes) {
			ans += t.toString() + ",";
		}
		char[] ansc = ans.toCharArray();
		if (ansc.length == 1) {
			return String.valueOf(ansc) + ")";
		}
		ansc[ansc.length-1] = ')';
		return String.valueOf(ansc);
	}
	
	public static String getParameterListAsString(ConstructorDeclaration md) {
		String ans = "(";
		List<Type> parameterTypes = new ArrayList<>();
		parameterTypes = extractMethodParameterTypes(md);
		for (Type t: parameterTypes) {
			ans += t.toString() + ",";
		}
		char[] ansc = ans.toCharArray();
		if (ansc.length == 1) {
			return String.valueOf(ansc) + ")";
		}
		ansc[ansc.length-1] = ')';
		return String.valueOf(ansc);
	}
	
//-------------------------------------------------------------------------------------------------------------------------------	
	public static void removeComments(MethodDeclaration method) {
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(LineComment comment, Void arg) {
                super.visit(comment, arg);
                comment.remove();
            }
        }, null);
    }
	
//-------------------------------------------------------------------------------------------------------------------------------	
	public static List<Type> extractMethodParameterTypes(MethodDeclaration method) { 
        List<Type> parameterTypes = new ArrayList<>();
        method.getParameters().forEach(parameter -> parameterTypes.add(parameter.getType()));
        return parameterTypes;
    }
	
	public static List<Type> extractMethodParameterTypes(ConstructorDeclaration method) { 
        List<Type> parameterTypes = new ArrayList<>();
        method.getParameters().forEach(parameter -> parameterTypes.add(parameter.getType()));
        return parameterTypes;
    }
}

package get;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

public class find {
	public static MethodDeclaration findSameNameMethod(CompilationUnit cu, String methodNameWithoutPara) {// search method(with same name)
		List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
		for (MethodDeclaration method : methods) {
			// get method's name
            String methodName = method.getNameAsString();
            if (methodName.equals(methodNameWithoutPara)){
            	return method;
            }
        }
        return null;
    }
	
	public static MethodDeclaration findSameNameWithSameNumOfParaMethod(CompilationUnit cu, String methodNameWithPara, int numOfPara) {// search method(with same name and same number of parameters)
		String methodNameWithoutPara;
		int index = -1;
		for (int i = 0; i<methodNameWithPara.length(); i++) {
			if (methodNameWithPara.charAt(i) == '(') {
				index = i;
				break;
			}
		}
		methodNameWithoutPara = methodNameWithPara.substring(0, index);
		
		List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
		
		for (MethodDeclaration method : methods) {
			// get method's name
            String pmethodNameWithPara = get.getMethodNameWithParaFromDeclaration(method);
            String pmethodNameWithoutPara = get.getMethodNameFromDeclaration(method);
            int num = get.countParameters(pmethodNameWithPara);
            if (pmethodNameWithoutPara.equals(methodNameWithoutPara) && !pmethodNameWithPara.equals(methodNameWithPara) && num ==numOfPara){
            	return method;
            }
        }
        return null;
    }
	
	public static MethodDeclaration findSameNameWithParaMethod(CompilationUnit cu, String methodNameWithPara) {// search method(with same name and same parameters list)
		List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
		for (MethodDeclaration method : methods) {
            // get method's name
            String methodName = get.getMethodNameWithParaFromDeclaration(method);
            if (methodName.equals(methodNameWithPara)){
            	return method;
            }
        }
        return null;
    }
}

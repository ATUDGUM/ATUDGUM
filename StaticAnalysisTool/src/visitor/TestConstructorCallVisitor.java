package visitor;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class TestConstructorCallVisitor extends VoidVisitorAdapter<Void>{
	private String targetMethodName;
    private int numOfPara;
    private boolean isCalled;
    public List<String> testPlaces = new ArrayList<>();
	
	public TestConstructorCallVisitor(String targetMethodName, int num) {
        this.targetMethodName = targetMethodName;
        this.numOfPara = num;
        this.isCalled = false;
    }
	
	public boolean isCalled() {
        return isCalled;
    }
	
	@Override
    public void visit(ObjectCreationExpr n, Void arg) {
		super.visit(n, arg);
		String methodName;
		int left = -1;
		int right = -1;
		for (int i = 0; i < n.toString().length(); i++) {
			if (n.toString().charAt(i) == ' '){
				left = i+1;
			}
			if (n.toString().charAt(i) == '('){
				right = i;
				break;
			}
		}
		if(left == -1 || right == -1) {
			return ;
		}
		methodName = n.toString().substring(left, right);
		if(n.getArguments().size() == numOfPara && methodName.equals(targetMethodName)) {
			MethodDeclaration md = n.findAncestor(MethodDeclaration.class).orElse(null);
			String mn = md. getDeclarationAsString(false, false, true);
			testPlaces.add(mn);
			this.isCalled = true;
		}
	}
}

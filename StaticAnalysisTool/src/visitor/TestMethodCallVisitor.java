package visitor;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import get.get;

public class TestMethodCallVisitor extends VoidVisitorAdapter<Void>{
	private String targetMethodName;
    private boolean isCalled;
    public List<String> testPlaces = new ArrayList<>();
    public List<String> testContent = new ArrayList<>();
    
    public TestMethodCallVisitor(String targetMethodName, int num) {
        this.targetMethodName = targetMethodName;
        this.isCalled = false;
    }

    public boolean isCalled() {
        return isCalled;
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);
        if (n.getNameAsString().equals(targetMethodName)) {
        	MethodDeclaration md = n.findAncestor(MethodDeclaration.class).orElse(null);
        	get.removeComments(md);
        	testContent.add(md.toString());
        	if (md != null) {
                String mn = md. getDeclarationAsString(false, false, true);
                if(!testPlaces.contains(mn)) {
                	testPlaces.add(mn); 
                	
                }
            } 
            isCalled = true;
        }
    }
}

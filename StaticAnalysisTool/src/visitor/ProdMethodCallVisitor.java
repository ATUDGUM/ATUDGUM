package visitor;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import get.get;

public class ProdMethodCallVisitor extends VoidVisitorAdapter<Void>{
	private String targetMethodName;
    private int numOfPara;
    private boolean isCalled;
    public List<String> callPlaces = new ArrayList<>();
	
    public ProdMethodCallVisitor(String targetMethodName, int num) {
    	this.targetMethodName = targetMethodName;
        this.numOfPara = num;
        this.isCalled = false;
    }
    
    public boolean isCalled() {
        return isCalled;
    }
    
    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);
        if (n.getNameAsString().equals(targetMethodName) && n.getArguments().size() == numOfPara) {
        	MethodDeclaration md = n.findAncestor(MethodDeclaration.class).orElse(null);
        	if (md != null) {
                String mn = get.getMethodNameWithParaFromDeclaration(md);
                if(!callPlaces.contains(mn)) {
                	callPlaces.add(mn); 
                }
            } 
            isCalled = true;
        }
    }
}

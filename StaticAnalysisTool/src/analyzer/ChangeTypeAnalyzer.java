package analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import get.find;
import get.get;
import visitor.MethodVisitor;
import visitor.ProdMethodCallVisitor;
import visitor.TestConstructorCallVisitor;
import visitor.TestMethodCallVisitor;

public class ChangeTypeAnalyzer {
	private String bp;
	private String ap;
	private String bt; 
	
	private List<SourceCodeChange> pChanges;
	
	private CompilationUnit beforeProdClass;
	private CompilationUnit afterProdClass;
	private CompilationUnit beforeTestClass;
	
	private HashMap<String, String> methodChangeType = new HashMap<String, String>();
	
	private List<String> changeTypes = new ArrayList<>();
	
	public ChangeTypeAnalyzer() {
		
	}
	
	public ChangeTypeAnalyzer(String bp, String ap, String bt) {
		this.bp = bp;
		this.ap = ap;
		this.bt = bt;
	}
	
	private void analyzeChanges() throws FileNotFoundException {
		pChanges = producChanges(bp, ap);
		
		beforeProdClass = StaticJavaParser.parse(new File(bp));
		afterProdClass = StaticJavaParser.parse(new File(ap));
		beforeTestClass = StaticJavaParser.parse(new File(bt));
        
		int numOfAddPara = 0;
		SourceCodeChange change;
		for (int i = 0; i < pChanges.size(); i++) {
			change = pChanges.get(i);
			String changeUniqueName = change.getRootEntity().getUniqueName();
			switch (change.getChangeType()) {
				case ADDITIONAL_FUNCTIONALITY:
					analyzeMethodAdd(change);
					break;
				case REMOVED_FUNCTIONALITY:
					analyzeMethodRemove(change);
					break;
				case PARAMETER_INSERT:
					numOfAddPara++;
					if (i == pChanges.size()-1){
						analyzeParameterChange(change, numOfAddPara);
						numOfAddPara = 0;
					}
					else if(!(pChanges.get(i + 1).getRootEntity().getUniqueName().equals(changeUniqueName) 
							&& (pChanges.get(i + 1).getChangeType().toString().equals("PARAMETER_INSERT") 
									|| pChanges.get(i + 1).getChangeType().toString().equals("PARAMETER_DELETE") 
									|| pChanges.get(i + 1).getChangeType().toString().equals("PARAMETER_TYPE_CHANGE")))) {
						analyzeParameterChange(change, numOfAddPara);
						numOfAddPara = 0;
					}
					break;
				case PARAMETER_DELETE:
					numOfAddPara--;
					if (i == pChanges.size()-1){
						analyzeParameterChange(change, numOfAddPara);
						numOfAddPara = 0;
					}
					else if(!(pChanges.get(i + 1).getRootEntity().getUniqueName().equals(changeUniqueName) 
							&& (pChanges.get(i + 1).getChangeType().toString().equals("PARAMETER_INSERT") 
									|| pChanges.get(i + 1).getChangeType().toString().equals("PARAMETER_DELETE") 
									|| pChanges.get(i + 1).getChangeType().toString().equals("PARAMETER_TYPE_CHANGE")))) {
						analyzeParameterChange(change, numOfAddPara);
						numOfAddPara = 0;
					}
					break;
				case PARAMETER_TYPE_CHANGE:
					if (i == pChanges.size()-1){
						analyzeParameterChange(change, numOfAddPara);
						numOfAddPara = 0;
					}
					else if(!(pChanges.get(i + 1).getRootEntity().getUniqueName().equals(changeUniqueName) 
							&& (pChanges.get(i + 1).getChangeType().toString().equals("PARAMETER_INSERT") 
									|| pChanges.get(i + 1).getChangeType().toString().equals("PARAMETER_DELETE") 
									|| pChanges.get(i + 1).getChangeType().toString().equals("PARAMETER_TYPE_CHANGE")))) {
						analyzeParameterChange(change, numOfAddPara);
						numOfAddPara = 0;
					}
					break;
				case METHOD_RENAMING:
					analyzeMethodRename(change);
					break;
				case RETURN_TYPE_CHANGE:
					analyzeReturnTypeChange(change);
					break;
				default:
					break;
			}
		}
		analyseMethodRename();
		analyseParameterTypeChange();
		analyseContentChange();
		analyseMethodRemove();
		analyseMethodAdd();
	}
	
	private void analyzeMethodAdd(SourceCodeChange change) {
		if(!change.getChangedEntity().isPrivate()) {
			String methodNameWithPara = get.getMethodNameFromEntity(change.getChangedEntity());// such as query(String)
			
			String mod = "";
			if (find.findSameNameWithParaMethod(afterProdClass, methodNameWithPara) != null) {
				mod = get.getModifierFromDeclaration(find.findSameNameWithParaMethod(afterProdClass, methodNameWithPara));
			}else {
				mod = get.getModifierFromEntity(change.getChangedEntity());
			}
			
			if (isChanged(mod, methodNameWithPara)) {
				return ;
			}
			
			String re;
			re = "[MethodAdded]ProductionClass add new " + mod + "method: " + methodNameWithPara;
			if (!changeTypes.contains(re)) {
				changeTypes.add(re);
				methodChangeType.put(mod + methodNameWithPara, re);
			}
		}
		return ;
	}
	
	private void analyseMethodAdd() {
        List<MethodDeclaration> oldMethods = beforeProdClass.findAll(MethodDeclaration.class);
        List<MethodDeclaration> newMethods = afterProdClass.findAll(MethodDeclaration.class);
		
        List<ConstructorDeclaration> oldConstructorMethods = beforeProdClass.findAll(ConstructorDeclaration.class);
        List<ConstructorDeclaration> newConstructorMethods = afterProdClass.findAll(ConstructorDeclaration.class);
        
        for (ConstructorDeclaration newMethod: newConstructorMethods) {
        	if (newMethod.isPrivate()){
        		continue;
        	}
        	boolean flag =true;
        	for (ConstructorDeclaration oldMethod: oldConstructorMethods) {
        		if(get.isSameMethod(oldMethod, newMethod)) { 
        			flag = false;
        			break;
        		}
        	}
        	if (flag) {
        		String methodNameWithPara = get.getMethodNameWithParaFromDeclaration(newMethod);
        		String mod = get.getModifierFromDeclaration(newMethod);
        		
        		if (isChanged(mod, methodNameWithPara)) {
    				continue ;
    			}
        		
        		String re;
        		re = "[MethodAdded]ProductionClass add new " + mod + "Constructor method: " + methodNameWithPara;
				if(!changeTypes.contains(re)) {
					changeTypes.add(re);
					methodChangeType.put(mod + methodNameWithPara, re);
				}
        	}
        }
        
        
        for (MethodDeclaration newMethod: newMethods) {
        	if(newMethod.isPrivate()){	
        		continue;
        	}
        	
        	boolean flag =true;
        	for (MethodDeclaration oldMethod: oldMethods) {
        		if(get.isSameMethod(oldMethod, newMethod)) {      
        			flag = false;
        			break;
        		}
        	}
        	
        	if (flag) {
        		String methodNameWithPara = get.getMethodNameWithParaFromDeclaration(newMethod);
        		String mod = get.getModifierFromDeclaration(newMethod);
        		
        		if (isChanged(mod, methodNameWithPara)) {
    				continue ;
    			}
        		
        		String re;
        		re = "[MethodAdded]ProductionClass add new " + mod +"method: " + methodNameWithPara;
				
				if(!changeTypes.contains(re)) {
					changeTypes.add(re);
					methodChangeType.put(mod + methodNameWithPara, re);
				}
        	}
        }
		return ; 
	}
	
	private void analyzeMethodRemove(SourceCodeChange change) {
		if(!change.getChangedEntity().isPrivate()) { 
			String methodNameWithPara = get.getMethodNameFromEntity(change.getChangedEntity());
			String methodNameWithoutPara;
			int numOfPara = get.countParameters(methodNameWithPara);
			int index = -1;
			for (int i = 0; i<methodNameWithPara.length(); i++) {
				if (methodNameWithPara.charAt(i) == '(') {
					index = i;
					break;
				}
			}
			methodNameWithoutPara = methodNameWithPara.substring(0, index);
			
			String mod = "";
			if (find.findSameNameWithParaMethod(afterProdClass, methodNameWithPara) != null) {
				mod = get.getModifierFromDeclaration(find.findSameNameWithParaMethod(afterProdClass, methodNameWithPara));
			}else {
				mod = get.getModifierFromEntity(change.getChangedEntity());
			}
			
			
			if (isChanged(mod, methodNameWithPara)) {
				return ;
			}
			
			TestMethodCallVisitor visitor = new TestMethodCallVisitor(methodNameWithoutPara, numOfPara);
			visitor.visit(beforeTestClass, null);
			String re;
			if(visitor.isCalled()) {
				re = "[MethodRemoved]ProductionClass remove " + mod + "method: " + methodNameWithPara;
				if (!changeTypes.contains(re)) {
					changeTypes.add(re);
					methodChangeType.put(mod + methodNameWithPara, re);
				}
			}		
		}
		return ;
	}
	
	private void analyseMethodRemove() {
        List<MethodDeclaration> oldMethods = beforeProdClass.findAll(MethodDeclaration.class);
        List<MethodDeclaration> newMethods = afterProdClass.findAll(MethodDeclaration.class);
		
        List<ConstructorDeclaration> oldConstructorMethods = beforeProdClass.findAll(ConstructorDeclaration.class);
        List<ConstructorDeclaration> newConstructorMethods = afterProdClass.findAll(ConstructorDeclaration.class);
        
        for (ConstructorDeclaration oldMethod: oldConstructorMethods) {
        	if (oldMethod.isPrivate()){
        		continue;
        	}
        	boolean flag =true;
        	for (ConstructorDeclaration newMethod: newConstructorMethods) {
        		if(get.isSameMethod(oldMethod, newMethod)) { 
        			flag = false;
        			break;
        		}
        	}
        	if (flag) {
        		String methodNameWithPara = get.getMethodNameWithParaFromDeclaration(oldMethod);
        		String methodNameWithoutPara = get.getMethodNameFromDeclaration(oldMethod);
        		int numOfPara = get.countParameters(methodNameWithPara);
        		String mod = get.getModifierFromDeclaration(oldMethod);
        		
        		if (isChanged(mod, methodNameWithPara)) {
    				continue ;
    			}
        		
        		TestConstructorCallVisitor visitor = new TestConstructorCallVisitor(methodNameWithoutPara, numOfPara);
    			visitor.visit(beforeTestClass, null);
    			String re;
    			if(visitor.isCalled()) {
    				re = "[MethodRemoved]ProductionClass remove " + mod + "Constructor method: " + methodNameWithPara;
    				if (!changeTypes.contains(re)) {
    					changeTypes.add(re);
    					methodChangeType.put(mod + methodNameWithPara, re);
    				}
    			}
        	
        	}
        }
        
        
        for (MethodDeclaration oldMethod: oldMethods) {
        	if(oldMethod.isPrivate()){	
        		continue;
        	}
        	
        	boolean flag =true;
        	for (MethodDeclaration newMethod: newMethods) {
        		if(get.isSameMethod(oldMethod, newMethod)) {        
        			flag = false;
        			break;
        		}
        	}
        	
        	if (flag) {
        		String methodNameWithoutPara = get.getMethodNameFromDeclaration(oldMethod);
        		String methodNameWithPara = get.getMethodNameWithParaFromDeclaration(oldMethod);
        		int numOfPara = get.countParameters(methodNameWithPara);
        		String mod = get.getModifierFromDeclaration(oldMethod);
        		
        		if (isChanged(mod, methodNameWithPara)) {
    				continue ;
    			}
        		
        		TestMethodCallVisitor visitor = new TestMethodCallVisitor(methodNameWithoutPara, numOfPara);
        		visitor.visit(beforeTestClass, null);
        		String re;
    			if(visitor.isCalled()) {
    				re = "[MethodRemoved]ProductionClass remove " + mod +"method: " + methodNameWithPara;
    				if(!changeTypes.contains(re)) {
    					changeTypes.add(re);
    					methodChangeType.put(mod + methodNameWithPara, re);
    				}
    			}
        	}
        }
		return ; 
	}

	private void analyzeParameterChange(SourceCodeChange change, int numOfAddPara) {
		if(change.getRootEntity().isPrivate()) {
			return ;
		}
		String methodNameWithPara = get.getMethodNameFromRootEntity(change.getRootEntity());
		String methodNameWithoutPara;
		int numOfPara = get.countParameters(methodNameWithPara);
		int index = -1;
		for (int i = 0; i<methodNameWithPara.length(); i++) {
			if (methodNameWithPara.charAt(i) == '(') {
				index = i;
				break;
			}
		}
		methodNameWithoutPara = methodNameWithPara.substring(0, index);
		
		String mod = "";
		if (find.findSameNameWithParaMethod(afterProdClass, methodNameWithPara) != null) {
			mod = get.getModifierFromDeclaration(find.findSameNameWithParaMethod(afterProdClass, methodNameWithPara));
		}else {
			mod = get.getModifierFromEntity(change.getRootEntity());
		}
		
		if (isChanged(mod, methodNameWithPara)) {
			return ;
		}
		
		MethodDeclaration bmd = find.findSameNameWithSameNumOfParaMethod(beforeProdClass, methodNameWithPara, numOfPara - numOfAddPara);
		MethodDeclaration amd = find.findSameNameMethod(afterProdClass, methodNameWithoutPara);
		if (bmd == null || amd == null) {
			return ;
		}
		String beforeList = bmd.getParameters().stream().map(Parameter -> Parameter.getType()).collect(Collectors.toList()).toString();
		String afterList = amd.getParameters().stream().map(Parameter -> Parameter.getType()).collect(Collectors.toList()).toString();
		String re;
	
        re = "[ParameterListChanged]ProductionClass " + mod + "method: " + methodNameWithoutPara + " changed its parameterlist from " + beforeList + " to " + afterList;
		
		TestMethodCallVisitor visitor = new TestMethodCallVisitor(methodNameWithoutPara, numOfPara-numOfAddPara);
		visitor.visit(beforeTestClass, null);
		if(visitor.isCalled()) {
			if(!changeTypes.contains(re)) {
				changeTypes.add(re);
				methodChangeType.put(get.getModifierFromDeclaration(bmd) + get.getMethodNameWithParaFromDeclaration(bmd), re);
				methodChangeType.put(get.getModifierFromDeclaration(amd) + get.getMethodNameWithParaFromDeclaration(amd), re);
			}
		}
		
		re = "[ParameterListChanged]ProductionClass " + mod + "constructor method: " + methodNameWithoutPara + " changed its parameterlist from " + beforeList + " to " + afterList;
		
		TestConstructorCallVisitor visitor1 = new TestConstructorCallVisitor(methodNameWithoutPara, numOfPara-numOfAddPara);
		visitor1.visit(beforeTestClass, null);
		if(visitor1.isCalled()) {
			if(!changeTypes.contains(re)) {
				changeTypes.add(re);
				methodChangeType.put(mod + methodNameWithPara, re);
			}
		}
		return ;
	}
	
	private void analyseParameterTypeChange() {
        List<MethodDeclaration> oldMethods = beforeProdClass.findAll(MethodDeclaration.class);
        List<MethodDeclaration> newMethods = afterProdClass.findAll(MethodDeclaration.class);
        
        for (MethodDeclaration oldMethod : oldMethods) {
        	if (oldMethod.isPrivate()) {
        		continue;
        	}
        	boolean isUnchanged = false;
        	for (MethodDeclaration newMethod : newMethods) {
        		if(get.isSameMethod(oldMethod, newMethod)) {
        			isUnchanged = true;
        		}
        	}
        	if (isUnchanged) {
        		continue;
        	}
        	
            for (MethodDeclaration newMethod : newMethods) {
            	if (oldMethods.contains(newMethod)) {
            		continue;
            	}
            	if (newMethod.isPrivate()) {
            		continue;
            	}
                if (oldMethod.getName().equals(newMethod.getName()) 
                		&& oldMethod.getParameters().size() == newMethod.getParameters().size() 
                		&& get.getModifierFromDeclaration(oldMethod).equals(get.getModifierFromDeclaration(newMethod))) {
                    if(!oldMethod.getParameters().equals(newMethod.getParameters())) {
                    	int numOfPara = newMethod.getParameters().size();
                    	int numOfAddPara = numOfPara-oldMethod.getParameters().size();
                    	String methodNameWithoutPara = get.getMethodNameFromDeclaration(newMethod);
                    	String mod = get.getModifierFromDeclaration(oldMethod);
                    	String re;
                    	
                    	String beforeList = oldMethod.getParameters().stream().map(Parameter -> Parameter.getType()).collect(Collectors.toList()).toString();
                		String afterList = newMethod.getParameters().stream().map(Parameter -> Parameter.getType()).collect(Collectors.toList()).toString();
                    	
                    	if(isChanged(get.getModifierFromDeclaration(oldMethod), get.getMethodNameWithParaFromDeclaration(oldMethod)) || 
                    			isChanged(get.getModifierFromDeclaration(newMethod), get.getMethodNameWithParaFromDeclaration(newMethod))) {
                    		continue;
                    	}
                    	
                    	re = "[ParameterListChanged]ProductionClass " + mod + "method: " + methodNameWithoutPara + " changed its parameterlist from " + beforeList + " to " + afterList;
                		
                		TestMethodCallVisitor visitor = new TestMethodCallVisitor(methodNameWithoutPara, numOfPara-numOfAddPara);
                		visitor.visit(beforeTestClass, null);
                		if(visitor.isCalled()) {
                			if(!changeTypes.contains(re)) {
                				changeTypes.add(re);
                				methodChangeType.put(get.getModifierFromDeclaration(oldMethod) + get.getMethodNameWithParaFromDeclaration(oldMethod), re);
                				methodChangeType.put(get.getModifierFromDeclaration(newMethod) + get.getMethodNameWithParaFromDeclaration(newMethod), re);
                			}
                		}
                		
                		re = "[ParameterListChanged]ProductionClass " + mod + "constructor method: " + methodNameWithoutPara + " changed its parameterlist from " + beforeList + " to " + afterList;
                		
                		TestConstructorCallVisitor visitor1 = new TestConstructorCallVisitor(methodNameWithoutPara, numOfPara-numOfAddPara);
                		visitor1.visit(beforeTestClass, null);
                		if(visitor1.isCalled()) {
                			if(!changeTypes.contains(re)) {
                				changeTypes.add(re);
                				methodChangeType.put(get.getModifierFromDeclaration(oldMethod) + get.getMethodNameWithParaFromDeclaration(oldMethod), re);
                				methodChangeType.put(get.getModifierFromDeclaration(newMethod) + get.getMethodNameWithParaFromDeclaration(newMethod), re);
                			}
                		}
                    }
                    else {
                    	break;
                    }
                }
            }
        }
		return ;
	}
	
	private void analyzeMethodRename(SourceCodeChange change) { 
		if(change.getRootEntity().isPrivate()) {
			return ;
		}
		String oldNameWithPara = get.getMethodNameFromEntity(change.getChangedEntity());
		String newNameWithPara = get.getMethodNameFromRootEntity(change.getRootEntity());
		int oldNumOfPara = get.countParameters(oldNameWithPara);
		String oldNameWithoutPara = oldNameWithPara.split("\\(")[0];
		
		String mod = "";
		if (find.findSameNameWithParaMethod(afterProdClass, newNameWithPara) != null) {
			mod = get.getModifierFromDeclaration(find.findSameNameWithParaMethod(afterProdClass, newNameWithPara));
		}else {
			mod = get.getModifierFromEntity(change.getRootEntity());
		}
		
		TestMethodCallVisitor visitor = new TestMethodCallVisitor(oldNameWithoutPara, oldNumOfPara);
		visitor.visit(beforeTestClass, null);
		
		String re = "";
		re = "[MethodRenamed]ProductionClass " + mod + "method: " + oldNameWithPara + " changed its name to " + newNameWithPara;
		
		if(visitor.isCalled()) {
			if(!changeTypes.contains(re)) {
				changeTypes.add(re);
				methodChangeType.put(get.getModifierFromDeclaration(find.findSameNameWithParaMethod(beforeProdClass, oldNameWithPara)) + oldNameWithPara, re);
				methodChangeType.put(get.getModifierFromDeclaration(find.findSameNameWithParaMethod(afterProdClass, newNameWithPara)) + newNameWithPara, re);
			}
		}
		return ;
	}
	
	private void analyseMethodRename() {
        List<MethodDeclaration> oldMethods = beforeProdClass.findAll(MethodDeclaration.class);
        List<MethodDeclaration> newMethods = afterProdClass.findAll(MethodDeclaration.class);
        
        for (MethodDeclaration oldMethod : oldMethods) {
        	if (oldMethod.isPrivate()) {
        		continue;
        	}
        	boolean isUnchanged = false;
        	for (MethodDeclaration newMethod : newMethods){
        		get.removeComments(oldMethod);
        		get.removeComments(newMethod);
        		if(oldMethod.equals(newMethod)) {
        			isUnchanged = true;
        		}
        	}
        	if (isUnchanged) {
        		continue;
        	}
            for (MethodDeclaration newMethod : newMethods) {
            	if (oldMethods.contains(newMethod)) {
            		continue;
            	} 
            	
            	if (newMethod.isPrivate()) {
            		continue;
            	}
            	
            	if (!oldMethod.getName().equals(newMethod.getName()) && oldMethod.getParameters().equals(newMethod.getParameters())) {
            		get.removeComments(oldMethod);
            		get.removeComments(newMethod);
            		if(oldMethod.getBody().equals(newMethod.getBody())) {
            			int numOfPara = oldMethod.getParameters().size();
                    	String oldmethodNameWithoutPara = get.getMethodNameFromDeclaration(oldMethod);
                    	String oldmethodNameWithPara = get.getMethodNameWithParaFromDeclaration(oldMethod);
            			String mod = get.getModifierFromDeclaration(oldMethod);
                    	
                    	TestMethodCallVisitor visitor = new TestMethodCallVisitor(oldmethodNameWithoutPara, numOfPara);
            			visitor.visit(beforeTestClass, null);
            			
            			String newmethodNameWithPara = get.getMethodNameWithParaFromDeclaration(newMethod);
            			
            			if(isChanged(mod, newmethodNameWithPara)) {
            				continue;
            			}
            			
            			String re = "";
            			
            			re = "[MethodRenamed]ProductionClass " + mod +"method: " + oldmethodNameWithPara + " changed its name to " + newmethodNameWithPara;
            			
            			if(visitor.isCalled()) {
                			if(!changeTypes.contains(re)) {
                				changeTypes.add(re);
                				methodChangeType.put(get.getModifierFromDeclaration(oldMethod) + get.getMethodNameWithParaFromDeclaration(oldMethod), re);
                				methodChangeType.put(get.getModifierFromDeclaration(newMethod) + get.getMethodNameWithParaFromDeclaration(newMethod), re);
                			}
            			}
            		}
            	}
            }
        }
		return;
	}
	
	private void analyzeReturnTypeChange(SourceCodeChange change) {
		if(change.getRootEntity().isPrivate()) {
			return ;
		}
		
		String methodNameWithPara = get.getMethodNameFromRootEntity(change.getRootEntity());
		int numOfPara = get.countParameters(methodNameWithPara);
		String methodNameWithoutPara; 
		int index = -1;
		for (int i = 0; i<methodNameWithPara.length(); i++) {
			if (methodNameWithPara.charAt(i) == '(') {
				index = i;
				break;
			}
		}
		methodNameWithoutPara = methodNameWithPara.substring(0, index);
		
		String mod = "";
		if (find.findSameNameWithParaMethod(afterProdClass, methodNameWithPara) != null) {
			mod = get.getModifierFromDeclaration(find.findSameNameWithParaMethod(afterProdClass, methodNameWithPara));
		}else {
			mod = get.getModifierFromEntity(change.getRootEntity());
		}
		
		TestMethodCallVisitor visitor = new TestMethodCallVisitor(methodNameWithoutPara, numOfPara);
		visitor.visit(beforeTestClass, null);
		
		String oldType = change.getChangedEntity().toString().split(": ")[change.getChangedEntity().toString().split(": ").length - 1];
		String newType;
		
		MethodVisitor mv =	new MethodVisitor(methodNameWithoutPara, numOfPara);
		mv.visit(afterProdClass, null);
		if(mv.getFoundMethod() == null) {
			newType = "";
		}else {
			newType = mv.getFoundMethod().getTypeAsString();
		}
		
		if (visitor.isCalled()){
			String re;
			re = "[MethodReturnTypeChanged]ProductionClass " + mod + "method: " + methodNameWithPara + " changed its returntype from " + oldType + " to " + newType;
			changeTypes.add(re);
			methodChangeType.put(mod + methodNameWithPara, re);
		}
		return ;
	}

	private void analyseContentChange() {
        List<MethodDeclaration> oldMethods = beforeProdClass.findAll(MethodDeclaration.class);
        List<MethodDeclaration> newMethods = afterProdClass.findAll(MethodDeclaration.class);
        
        for (MethodDeclaration oldMethod : oldMethods) {
        	if (oldMethod.isPrivate()){
        		continue;
        	}
        	
        	boolean isUnchanged = false;
        	for (MethodDeclaration newMethod : newMethods){
        		get.removeComments(oldMethod);
        		get.removeComments(newMethod);
        		if(oldMethod.equals(newMethod)) {
        			isUnchanged = true;
        		}
        	}
        	if (isUnchanged) {
        		continue;
        	}
        	
            for (MethodDeclaration newMethod : newMethods) {
            	if (oldMethods.contains(newMethod)) {
            		continue;
            	}
            	if (newMethod.isPrivate()) {
            		continue;
            	}
            	get.removeComments(oldMethod);
            	get.removeComments(newMethod);
            	if (oldMethod.getDeclarationAsString(true, false, false).equals(newMethod.getDeclarationAsString(true, false, false)) && !oldMethod.getBody().equals(newMethod.getBody())){
            		String methodNameWithPara = get.getMethodNameWithParaFromDeclaration(newMethod);
            		String methodNameWithoutPara = get.getMethodNameFromDeclaration(newMethod);
            		int numOfPara = get.countParameters(methodNameWithPara);
            		String mod = get.getModifierFromDeclaration(newMethod);
            		
            		if (isChanged(mod, methodNameWithPara)) {
            			continue;
            		}
            		
            		TestMethodCallVisitor visitor = new TestMethodCallVisitor(methodNameWithoutPara, numOfPara);
            		visitor.visit(beforeTestClass, null);
            	
            		if (visitor.isCalled()) { 
            			String re;
            			re = "[MethodContentChanged]ProductionClass " + mod + "method: " + methodNameWithPara + " changed its content" ;
            			changeTypes.add(re);
            			methodChangeType.put(get.getModifierFromDeclaration(oldMethod) + get.getMethodNameWithParaFromDeclaration(oldMethod), re);
        				methodChangeType.put(get.getModifierFromDeclaration(newMethod) + get.getMethodNameWithParaFromDeclaration(newMethod), re);
            		}
            		
            	}
            }
        }
        
        for (MethodDeclaration oldMethod : oldMethods) {
        	boolean isUnchanged = false;
        	for (MethodDeclaration newMethod : newMethods){
        		get.removeComments(oldMethod);
        		get.removeComments(newMethod);
        		if(oldMethod.equals(newMethod)) {
        			isUnchanged = true;
        		}
        	}
        	if (isUnchanged) {
        		continue;
        	}
        	
        	for (MethodDeclaration newMethod : newMethods) {
        		if (oldMethods.contains(newMethod)) {
            		continue;
            	}
            	get.removeComments(oldMethod);
            	get.removeComments(newMethod);
            	if (oldMethod.getDeclarationAsString().equals(newMethod.getDeclarationAsString()) && !oldMethod.getBody().equals(newMethod.getBody())) {
            		String methodNameWithPara = get.getMethodNameWithParaFromDeclaration(newMethod);
            		String methodNameWithoutPara = get.getMethodNameFromDeclaration(newMethod);
            		int numOfPara = get.countParameters(methodNameWithPara);
            		
            		ProdMethodCallVisitor visitor = new ProdMethodCallVisitor(methodNameWithoutPara, numOfPara);
            		visitor.visit(beforeProdClass, null);
            		
            		ProdMethodCallVisitor visitor1 = new ProdMethodCallVisitor(methodNameWithoutPara, numOfPara);
            		visitor1.visit(afterProdClass, null);
            		
            		if (visitor.isCalled() && visitor1.isCalled()) { 
            			for (String str : visitor.callPlaces){ 
            				if (visitor1.callPlaces.contains(str)) {
            					String ProdMethodNameWithPara = str;
            					String ProdMethodNameWithoutPara;
            					int idx= -1;
            					for (int i = 0; i<ProdMethodNameWithPara.length(); i++) {
            						if (ProdMethodNameWithPara.charAt(i) == '('){
            							idx = i;
            						}
            					}
            					if (idx == -1) {
            						continue;
            					}
            					ProdMethodNameWithoutPara = ProdMethodNameWithPara.substring(0, idx);
            					int numOfProdPara = get.countParameters(ProdMethodNameWithPara);
            					
            					MethodVisitor vi = new MethodVisitor(ProdMethodNameWithoutPara, numOfProdPara);
            					vi.visit(afterProdClass, null);
            					
            					if (vi.getFoundMethod().isPrivate()) {
            						continue;
            					}
            					
            					if (vi.getFoundMethod().isPublic() && isChanged("Public ", ProdMethodNameWithPara)) {
            						continue;
            					}
            					else if(vi.getFoundMethod().isProtected() && isChanged("Protected ", ProdMethodNameWithPara)){
            						continue;
            					}
            					else if(isChanged("", ProdMethodNameWithPara)){
            						continue;
            					}
            					
            					TestMethodCallVisitor vi2 = new TestMethodCallVisitor(ProdMethodNameWithoutPara, numOfProdPara);
            					vi2.visit(beforeTestClass, null);
            					
            					String re;
                    			if (vi.getFoundMethod().isPublic()){
                    				re = "[MethodContentChanged]ProductionClass Public method: " + ProdMethodNameWithPara + " changed its content" ;
                    			}
                    			else if(vi.getFoundMethod().isProtected()){
                    				re = "[MethodContentChanged]ProductionClass Protected method: " + ProdMethodNameWithPara + " changed its content";
                    			}
                    			else {
                    				re = "[MethodContentChanged]ProductionClass method: " + ProdMethodNameWithPara + " changed its content";
                    			}
                    			
                    			changeTypes.add(re);
                    			if(vi.getFoundMethod().isPublic()) {
                    				methodChangeType.put("Public " + ProdMethodNameWithPara, re);
                    			}
                    			else if(vi.getFoundMethod().isProtected()){
                    				methodChangeType.put("Protected " + ProdMethodNameWithPara, re);
                    			}
                    			else {
                    				methodChangeType.put(ProdMethodNameWithPara, re);
                    			}
                    			
            				}
            			}
            		}
            		
            		
            	}
            	
        	}
        }
        
		return ;
	}
		
	private List<SourceCodeChange> producChanges(String bp, String ap) { 
		File left = new File(bp);
		File right = new File(ap);
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		try {
		    distiller.extractClassifiedSourceCodeChanges(left, right);
		} catch(Exception e) {
		    System.err.println("Warning: error while change distilling. " + e.getMessage());
		}
		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		return changes;
	}
	
	private boolean isChanged(String mod, String methodNameWithPara) {
		return methodChangeType.containsKey(mod + methodNameWithPara);
	}
	
	public void prinChanges() { 
		pChanges = producChanges(bp, ap);
		System.out.println("ProdClassChanges:");
		if(pChanges != null) {
			int idx = 1;
		    for(SourceCodeChange change : pChanges) {
		    	System.out.println(idx+".Change place: " + change.getRootEntity().toString());
		    	System.out.println("Change type: " + change.getChangeType().toString());
                System.out.println("Changed entity: " + change.getChangedEntity().toString());
                idx++;
		    }
		}
		System.out.println("");
	}
	
	public List<String> getChangeTypes() {
		try {
			analyzeChanges();
		}catch (FileNotFoundException e){
			System.out.println(e);
		}
		return changeTypes;
	}
}

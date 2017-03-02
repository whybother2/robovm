package org.robovm.compiler.plugin.debug;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.robovm.compiler.ModuleBuilder;
import org.robovm.compiler.clazz.Clazz;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Config.Builder;
import org.robovm.compiler.llvm.BasicBlock;
import org.robovm.compiler.llvm.DebugMetadata;
import org.robovm.compiler.llvm.Function;
import org.robovm.compiler.llvm.FunctionDeclaration;
import org.robovm.compiler.llvm.FunctionRef;
import org.robovm.compiler.llvm.FunctionType;
import org.robovm.compiler.llvm.Instruction;
import org.robovm.compiler.llvm.IntegerConstant;
import org.robovm.compiler.llvm.MetadataNode;
import org.robovm.compiler.llvm.MetadataString;
import org.robovm.compiler.llvm.NamedMetadata;
import org.robovm.compiler.llvm.Type;
import org.robovm.compiler.llvm.UnnamedMetadata;
import org.robovm.compiler.llvm.Value;
import org.robovm.compiler.llvm.debug.DICompileUnit;
import org.robovm.compiler.llvm.debug.DIFile;
import org.robovm.compiler.llvm.debug.DILexicalBlock;
import org.robovm.compiler.llvm.debug.DILocation;
import org.robovm.compiler.llvm.debug.DISubprogram;
import org.robovm.compiler.llvm.debug.map.DIMapValueReference;
import org.robovm.compiler.log.Logger;
import org.robovm.compiler.plugin.AbstractCompilerPlugin;
import org.robovm.compiler.plugin.PluginArgument;
import org.robovm.compiler.plugin.PluginArguments;

import soot.SootMethod;
import soot.Unit;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;

public class DebugInformationPlugin extends AbstractCompilerPlugin {
    private Logger log;
    private String[] sourcePath;
    
    private static FunctionRef LLVM_DBG_DECLARE_FUN = new FunctionRef("llvm.dbg.declare", new FunctionType(Type.VOID, new Type[] { Type.METADATA, Type.METADATA, Type.METADATA }));
    private static FunctionDeclaration LLVM_DBG_DECLARE_DECLARATION = new FunctionDeclaration(LLVM_DBG_DECLARE_FUN);
	private UnnamedMetadata diFileMeta;
	private UnnamedMetadata diCuMeta;
    
    public DebugInformationPlugin() {
	}
    
    public PluginArguments getArguments() {
    	return new PluginArguments("debug", Arrays.asList(new PluginArgument[] { new PluginArgument("sourcepath", "<list>", "A colon delimited list of paths containing the sources.") }));
    }
    
    @Override
    public void beforeConfig(Builder builder, Config config) throws IOException {
    	super.beforeConfig(builder, config);
    	
    	String sourcePathParam = parseArguments(config).get("sourcepath");
    	if (sourcePathParam != null) {
    		this.sourcePath = sourcePathParam.split(":");
    	}
    	else {
    		//TODO!
    		//this.sourcePath = 
    	}
    }
    
    @Override
    public void beforeClass(Config config, Clazz clazz, ModuleBuilder moduleBuilder) throws IOException {
    	super.beforeClass(config, clazz, moduleBuilder);
    	    	
    	DIFile diFile = new DIFile(getSourceFile(config, clazz));
    	DICompileUnit diCu = new DICompileUnit();
    	
    	diCuMeta = moduleBuilder.newUnnamedMetadata(diCu);
    	diFileMeta = moduleBuilder.newUnnamedMetadata(diFile);
    	diCu.setFile(new DIMapValueReference(diFileMeta));
    	
    	UnnamedMetadata dwarfVersion = moduleBuilder.newUnnamedMetadata(new MetadataNode(new Value[]{ new IntegerConstant(2), new MetadataString("Dwarf Version"), new IntegerConstant(2) }));
    	UnnamedMetadata debugInfoVersion = moduleBuilder.newUnnamedMetadata(new MetadataNode(new Value[]{ new IntegerConstant(2), new MetadataString("Debug Info Version"), new IntegerConstant(3) }));
    	UnnamedMetadata picLevel = moduleBuilder.newUnnamedMetadata(new MetadataNode(new Value[]{ new IntegerConstant(1), new MetadataString("PIC Level"), new IntegerConstant(2) }));
    	
    	moduleBuilder.addNamedMetadata(new NamedMetadata("llvm.db.cu", diCuMeta));
    	moduleBuilder.addNamedMetadata(new NamedMetadata("llvm.module.flags", new UnnamedMetadata[] { dwarfVersion, debugInfoVersion, picLevel }));
    	moduleBuilder.addFunctionDeclaration(LLVM_DBG_DECLARE_DECLARATION);
    }
    
    @Override
    public void afterMethod(Config config, Clazz clazz, SootMethod method, ModuleBuilder moduleBuilder, Function function) throws IOException {
    	super.afterMethod(config, clazz, method, moduleBuilder, function);
    	

        // don't try to generate shadow frames for native or abstract methods
        // or methods that don't have any instructions in them
        if (method.isNative() || method.isAbstract() || !method.hasActiveBody()) {
            return;
        }
        
        BasicBlock entryBlock = function.getBasicBlocks().get(0);
        
        //Method has only a return null statement
        if (entryBlock.getInstructions().size() == 1) {
        	return;
        }
        
        DISubprogram diSub = new DISubprogram();
        UnnamedMetadata diSubNode = moduleBuilder.newUnnamedMetadata(diSub);
        
        int methodLineNumber = 0;
        for (BasicBlock bb : function.getBasicBlocks()) {
            for (int i = 0; i < bb.getInstructions().size(); i++) {
                Instruction instruction = bb.getInstructions().get(i);
                List<Object> units = instruction.getAttachments();
                int currentLineNumber = -1;
                for (Object object : units) {
                    if (object instanceof Unit) {
                        Unit unit = (Unit) object;
                        
                        LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
                        if (tag != null) {
                        	currentLineNumber = tag.getLineNumber();
                        	methodLineNumber = Math.min(methodLineNumber, currentLineNumber);
                        	instruction.addMetadata(new DebugMetadata(moduleBuilder.newUnnamedMetadata(new DILocation(new DIMapValueReference(diSubNode), currentLineNumber, 0)).ref()));
                        }
                    }
                }
            }
        }
        
        diSub.setFile(new DIMapValueReference(diFileMeta));
        diSub.setScope(new DIMapValueReference(diFileMeta));
        diSub.setName(function.getName());
        
        function.addMetadata(new DebugMetadata(diSubNode.ref()));
        
        DILexicalBlock diBlock = new DILexicalBlock();
        diBlock.setFile(new DIMapValueReference(diFileMeta));
        diBlock.setScope(new DIMapValueReference(diSubNode));
        diBlock.setLine(methodLineNumber);
        diBlock.setColumn(0);
        moduleBuilder.newUnnamedMetadata(diBlock);
        
    }
    
    private File getSourceFile(Config config, Clazz clazz) {
    	File sourceFile = null;
    	SourceFileTag sourceFileTag = (SourceFileTag) clazz.getSootClass().getTag("SourceFileTag");
    	if (sourceFileTag == null) {
    		String className = clazz.getInternalName();
    		sourceFile = new File(className.substring(clazz.getInternalName().lastIndexOf("/") + 1) + ".java");
    	}
    	else {
    		sourceFile = new File(sourceFileTag.getSourceFile());
    	}
    	if (!sourceFile.exists() && sourcePath != null) {
    		for (String dir : sourcePath) {
    			sourceFile = new File(dir + "/" + sourceFile.getName());
    			if (sourceFile.exists()) {
    				return sourceFile;
    			}
    		}
    	}
    	
    	return sourceFile;
}
}
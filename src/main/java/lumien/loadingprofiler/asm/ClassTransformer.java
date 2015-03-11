package lumien.loadingprofiler.asm;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.POP;
import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ClassTransformer implements IClassTransformer
{
	Logger logger = LogManager.getLogger("LoadingProfiler");

	public ClassTransformer()
	{

	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (transformedName.equals("net.minecraftforge.fml.common.LoadController"))
		{
			// Patch Load Controller
			return patchLoadController(basicClass);
		}

		return basicClass;
	}

	private byte[] patchLoadController(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.INFO, "Patching FML Load Controller");
		logger.log(Level.INFO, "If you get any crashes while using this mod try to remove Loading Profiler before reporting them to the respective author.");

		MethodNode propogate = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals("propogateStateMessage"))
			{
				propogate = mn;
				break;
			}
		}

		if (propogate != null)
		{
			logger.log(Level.DEBUG, "- Found propogateStateMessage");

			for (int i = 0; i < propogate.instructions.size(); i++)
			{
				AbstractInsnNode node = propogate.instructions.get(i);
				if (node instanceof MethodInsnNode)
				{
					MethodInsnNode min = (MethodInsnNode) node;
					if (min.name.equals("sendEventToModContainer"))
					{
						logger.log(Level.DEBUG, "- Found sendEventToModContainer");

						InsnList insertBefore = new InsnList();
						InsnList insertAfter = new InsnList();

						insertBefore.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/loadingprofiler/Profiler", "pre", "(Lnet/minecraftforge/fml/common/event/FMLEvent;Lnet/minecraftforge/fml/common/ModContainer;)V",false));
						insertBefore.add(new VarInsnNode(ALOAD,1));
						insertBefore.add(new VarInsnNode(ALOAD,3));
						
						insertAfter.add(new VarInsnNode(ALOAD,1));
						insertAfter.add(new VarInsnNode(ALOAD,3));
						insertAfter.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/loadingprofiler/Profiler", "post", "(Lnet/minecraftforge/fml/common/event/FMLEvent;Lnet/minecraftforge/fml/common/ModContainer;)V",false));
					
						propogate.instructions.insertBefore(min, insertBefore);
						propogate.instructions.insert(min, insertAfter);
						
						break;
					}
				}
			}
		}
		else
		{
			logger.log(Level.DEBUG, "- Did not find propogateStateMessage");
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchDummyClass(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.INFO, "Found Dummy Class: " + classNode.name);

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}
}

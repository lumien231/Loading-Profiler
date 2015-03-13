package lumien.loadingprofiler.profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;
import net.minecraftforge.fml.common.event.FMLEvent;

public class TransformerProfiler
{
	static TransformerProfiler INSTANCE;

	HashMap<TransformerWrapper, Long> times;

	// Transformer Based Profiling
	Stack<TransformerWrapper> transformerStack;
	Stack<Long> timeStack;

	// Class Based Profiling
	Stack<String> classStack;
	HashMap<String, Long> classTimes;

	// Nested Time removal
	long nestedTime;
	boolean transformerRan;

	// State
	public static boolean enabled = true;

	private TransformerProfiler()
	{
		times = new HashMap<TransformerWrapper, Long>();
		transformerStack = new Stack<TransformerWrapper>();
		timeStack = new Stack<Long>();
		transformerRan = false;
		classStack = new Stack<String>();
		classTimes = new HashMap<String, Long>();
	}

	private void logPre(TransformerWrapper transformer, String transformedName)
	{
		if (transformerStack.isEmpty())
		{
			nestedTime = System.nanoTime();
			transformerRan = true;
		}

		classStack.push(transformedName);
		transformerStack.push(transformer);
		timeStack.push(System.nanoTime());
	}

	private void logPost(TransformerWrapper transformer, String transformedName)
	{
		long currentTimeDif = System.nanoTime() - timeStack.pop();
		TransformerWrapper currentTransformer = transformerStack.pop();
		String className = classStack.pop();

		if (transformerStack.isEmpty())
		{
			nestedTime = System.nanoTime() - nestedTime;
		}

		for (int i = 0; i < timeStack.size(); i++)
		{
			timeStack.set(i, timeStack.get(i) + currentTimeDif);
		}

		if (!className.equals(transformedName))
		{
			System.out.println("What");
		}
		else
		{
			if (classTimes.containsKey(className))
			{
				classTimes.put(className, classTimes.get(className) + currentTimeDif);
			}
			else
			{
				classTimes.put(className, currentTimeDif);
			}
		}

		if (currentTransformer != transformer)
		{
			System.out.println("What");
		}
		else
		{
			if (times.containsKey(transformer))
			{
				times.put(transformer, currentTimeDif + times.get(transformer));
			}
			else
			{
				times.put(transformer, currentTimeDif);
			}
		}
	}

	public static void preTransform(TransformerWrapper transformer, String transformedName)
	{
		if (enabled)
		{
			getProfiler().logPre(transformer, transformedName);
		}
	}

	public static void postTransform(TransformerWrapper transformer, String transformedName)
	{
		if (enabled)
		{
			getProfiler().logPost(transformer, transformedName);
		}
	}

	public static TransformerProfiler getProfiler()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new TransformerProfiler();
		}

		return INSTANCE;
	}

	public HashMap<TransformerWrapper, Long> getTimes()
	{
		return times;
	}

	public HashMap<String, Long> getClassTimes()
	{
		return classTimes;
	}
}

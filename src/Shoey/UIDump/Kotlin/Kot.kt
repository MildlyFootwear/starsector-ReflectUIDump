package Shoey.UIDump.Kotlin

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.state.AppDriver
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class Kot {

    private var log = Global.getLogger(this.javaClass)

    var s = ""
    var UIDump = ""
    var list: MutableList<String> = mutableListOf()
    var invokedList: MutableList<String> = mutableListOf()
    var depth: Int = 14
    var maxdepth: Int = 0;
    fun returnTypesString(subject: Any?) : String
    {
        var returnValue = ""
        if (subject == null)
        {
            return "null"
        }

        if (subject is Boolean)
        {
            returnValue += "Boolean:"
        }
        if (subject is Int)
        {
            returnValue += "Int:"
        }
        if (subject is Double)
        {
            returnValue += "Double:"
        }
        if (subject is List<*>) {
            returnValue += "List:"
        }

        if (subject is UIPanelAPI)
        {
            returnValue += "UIPanelAPI:"
        }
        if (subject is UIComponentAPI)
        {
            returnValue += "UIComponentAPI:"
        }
        if (subject is TooltipMakerAPI)
        {
            returnValue += "TooltipMakerAPI:"
        }
        if (subject is ButtonAPI)
        {
            returnValue += "ButtonAPI:"
        }
        if (subject is TooltipMakerAPI)
        {
            returnValue += "TooltipMakerAPI:"
        }
        if (subject is ShipAPI)
        {
            returnValue += "ShipAPI:"
        }
        if (subject is FleetMemberAPI)
        {
            returnValue += "FleetMemberAPI:"
        }
        if (subject is MutableShipStatsAPI)
        {
            returnValue += "MutableShipStatsAPI:"
        }
        if (subject is CampaignInputListener)
        {
            returnValue += "CampaignInputListener:"
        }
        if (subject is CampaignUIRenderingListener)
        {
            returnValue += "CampaignUIRenderingListener:"
        }
        return returnValue
    }

    fun hookCore(): UIPanelAPI?
    {
        var state = AppDriver.getInstance().currentState
        var core2 = invokeMethod("getCore", state)

        var dialog = invokeMethod("getEncounterDialog", state)
        if (dialog != null) {
            core2 = invokeMethod("getCoreUI", dialog)
        }
        return core2 as UIPanelAPI?
    }

    fun dump(toDump: UIPanelAPI?): String {
        list.clear()
        invokedList.clear()
        UIDump = ""
        var core = hookCore()
        if (toDump is UIPanelAPI)
            dumpDetails(toDump, "", true)
        else if (core != null)
            dumpDetails(core, "", true)
        return UIDump
    }

    fun dumpMethods(instance: Any, prefix: String, fromWithin: Boolean, invokeMethods: Boolean = true) {
        if (maxdepth < prefix.length/4)
            maxdepth = prefix.length/4
        if (s.length > 10000) {
            log.info("inserting "+s.length+" into "+UIDump.length+", max depth so far "+maxdepth)
            UIDump += s
            s = ""
        }
        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        if (fromWithin)
            s+="\n"+prefix+"Class: "+instance.javaClass.toString()
        for (m: Any in instancesOfMethods)
        {
            var methodName: String = getMethodNameHandle.invoke(m) as String
            var ident: String = instance.hashCode().toString()+methodName
            if (invokedList.contains(ident))
            {
                s+="\n$prefix"+"Method "+methodName+" has already been invoked on this instance."
                continue
            }
            s += "\n"+(prefix+"Method: "+methodName)
            if ((methodName.startsWith("get") || methodName.startsWith("is")) && !methodName.startsWith("getParent") && !invokedList.contains(ident) && invokeMethods && UIDump.length < 2000000)
            {
                invokedList.add(ident)
                try {
                    var invoked = invokeMethod(methodName, instance) as Any
                    s += "\n" + prefix + "Returns "+returnTypesString(invoked)+invoked.javaClass+": " + invoked.toString()
                    dumpText(invoked, prefix)

                    s += "\n" + prefix + "{"
                    dumpDetails(invoked, "    " + prefix, true, false)

                } catch (e: Exception)
                {
                    s += "\n" + prefix + e.localizedMessage

                }
            }
        }

    }

    fun dumpMethods(instance: Any, prefix: String) {
        dumpMethods(instance, prefix, false)
    }

    fun dumpText(instance: Any, prefix: String)
    {
        try {
            if (hasMethodOfName("getText", instance)) {
                s += "\n"+(prefix+"Text: " + (invokeMethod("getText", instance) as String?))
            }
        } catch (e: Exception)
        {
            s += "\n"+prefix+"Text: " + e.localizedMessage
        }

    }

    fun dumpDeclaredFields(instance: Any, prefix: String)
    {
        var list: List<String> = getFields(instance)
        if (list.size > 0)
        {
            s += "\n$prefix"+"Fields\n$prefix"+"{"
            for (f:String in list)
            {
                s += "\n$prefix    $f"
            }
            s+="\n$prefix}"
        } else {
            s += "\n$prefix"+"Fields Empty"
        }
    }

    fun dumpDetails(instance: Any, prefix: String, recursive: Boolean, skipMethod: Boolean) {
        if (maxdepth < prefix.length/4)
            maxdepth = prefix.length/4
        if (s.length > 10000) {
            log.info("inserting "+s.length+" into "+UIDump.length+", max depth so far "+maxdepth)
            UIDump += s
            s = ""
        }
        var cancel: Boolean = false
        try {

            s += "\n" + (prefix + "Class: " + instance.javaClass)
            s += "\n" + (prefix + "Types: " + returnTypesString(instance))

            dumpText(instance, prefix)
//            try {
//                dumpDeclaredFields(instance, prefix)
//            } catch (e: Exception) {
//            }

            if (prefix.length >= depth*4)
            {
                s += "\n"+prefix+"Max depth achieved at $depth, returning."
                return
            }
            if (!skipMethod) {
                cancel = true
                s += "\n" + (prefix) + "{"
                dumpMethods(instance, "    " + prefix, !recursive)
                s += "\n" + (prefix) + "}"
            }
        } catch (e: Exception) {
            cancel = true
            s += "\n"+e.localizedMessage
        }
        if (recursive && !cancel) {
            if (instance is UIPanelAPI) {
                s += "\n" + (prefix) + "{"
                for (c: UIComponentAPI in instance.getChildrenCopy())
                {
                    dumpDetails(c, "    " + prefix, true)
                }
                s += "\n" + (prefix) + "}"
            }
        }
        if (instance is UIPanelAPI)
        {
            list.add(list.size, instance.toString())
        }
        UIDump += s
        s = ""
    }
    fun dumpDetails(instance: Any, prefix: String, recursive: Boolean) {
        dumpDetails(instance, prefix, recursive, false)
    }
    fun dumpDetails(instance: Any, prefix: String)
    {
        dumpDetails(instance, prefix, true)
    }

    //Required to execute obfuscated methods without referencing their obfuscated class name.
    fun invokeMethod(methodName: String, instance: Any, vararg arguments: Any?): Any? {
        lateinit var method: Any

        val clazz = instance.javaClass
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        method = clazz.getMethod(methodName, *methodType.parameterArray())
        try {
            return invokeMethodHandle.invoke(method, instance, arguments)
        } catch (e: Exception) {
            return null
        }
    }



    fun getChildrenCopyFromHook(panelAPI: UIPanelAPI): List<UIComponentAPI> {
        return invokeMethod("getChildrenCopy", panelAPI) as List<UIComponentAPI>
    }

    fun getChildrenCopyFromHook(instance: Any): List<Any?> {
        return invokeMethod("getChildrenCopy", instance) as List<Any?>
    }

    //Extends the UI API by adding the required method to get the child objects of a panel, only when used within this class.
    private fun UIPanelAPI.getChildrenCopy(): List<UIComponentAPI> {
        return getChildrenCopyFromHook(this)
    }

    //Used to be able to find specific files without having to reference their obfuscated class name.
    private fun hasMethodOfName(name: String, instance: Any): Boolean {
        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        return instancesOfMethods.any { getMethodNameHandle.invoke(it) == name }
    }

    fun getFields(instance: Any): List<String> {

        try {
            var list: MutableList<String> = mutableListOf()
            val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredFields()
            for (fIn: Any in instancesOfMethods) {
                var s = getFieldNameHandle.invoke(fIn) as String
                try {
                    var v = getFieldHandle.invoke(fIn) as Any
                    if (v != null) {
                        list.add((s) + ": " + v)
                    }
                } catch (e: Exception) {
                    list.add((s) + ": couldn't retrieve. Exception "+e.localizedMessage)
                }
            }
            return list
        } catch (e: Exception) {
            var list: MutableList<String> = mutableListOf()
            list.add("Exception: "+e.localizedMessage)
            return list
        }
    }

    private val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
    private val setFieldHandle = MethodHandles.lookup()
        .findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Any::class.java, Any::class.java))
    private val setFieldAccessibleHandle = MethodHandles.lookup()
        .findVirtual(fieldClass, "setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

    val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    val getMethodNameHandle =
        MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
    val invokeMethodHandle = MethodHandles.lookup().findVirtual(
        methodClass,
        "invoke",
        MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java)
    )

    private val getFieldHandle = MethodHandles.lookup().findVirtual(fieldClass, "get", MethodType.methodType(Any::class.java, Any::class.java))
    private val getFieldTypeHandle = MethodHandles.lookup().findVirtual(fieldClass, "getType", MethodType.methodType(Class::class.java))
    private val getFieldNameHandle = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String::class.java))

}
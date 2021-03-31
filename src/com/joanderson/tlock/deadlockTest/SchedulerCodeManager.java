package com.joanderson.tlock.deadlockTest;

public class SchedulerCodeManager {
    public static String getSchedulerCode() {
        return
                "package scheduler\n" +
        "\n" +
        "import (\n" +
        "\t\"fmt\"\n" +
        "\t\"strconv\"\n" +
        ")\n" +
        "\n" +
        "// indicates the current time\n" +
        "var current_cycle = 0\n" +
        "\n" +
        "var lastElem = \"\" //Guarda o último elemento antes do deadlock (se) acontecer\n" +
        "\n" +
        "// increase the time by 1\n" +
        "func Tick() {\n" +
        "\tcurrent_cycle += 1\n" +
        "}\n" +
        "\n" +
        "// Scheduler status\n" +
        "type SchedState struct {\n" +
        "\tcompToWait     []string\n" +
        "\tunifications   map[string][]Unification\n" +
        "\tcomponents     map[string][]string\n" +
        "\tenabledActions []Action\n" +
        "\tenabledPorts   map[string]map[string]Port\n" +
        "\treplyChan      map[string]chan Reply\n" +
        "\tdoneComps      []string\n" +
        "\t//offers			map[string]CompOffer\n" +
        "}\n" +
        "\n" +
        "// Schedules the whole system:\n" +
        "// - initializes the state\n" +
        "// - loop:\n" +
        "//     * receive list of possible action for each component\n" +
        "//     * declare to the interface the components that terminated (done)\n" +
        "//     * compute possible communication actions (through unifications)\n" +
        "//     * let the interface choose an action among the possible ones\n" +
        "//     * execute the choosen action\n" +
        "func Scheduler(offersinput chan Offer, main_comp string, args []string) {\n" +
        "\tinteraction := parseArgs(args)\n" +
        "\tinteraction.init()\n" +
        "\tstate := SchedState{[]string{main_comp},\n" +
        "\t\tmap[string][]Unification{},\n" +
        "\t\tmap[string][]string{},\n" +
        "\t\t[]Action{},\n" +
        "\t\tmap[string]map[string]Port{main_comp: map[string]Port{}},\n" +
        "\t\tmap[string]chan Reply{},\n" +
        "\t\t[]string{}}\n" +
        "\ti := 1\n" +
        "\tfor {\n" +
        "\t\twaitOffers(&state, offersinput)\n" +
        "\t\tinteraction.emptyDoneComps(&state)\n" +
        "\t\tpossibleUnifs := listPossibleUnifications(state)\n" +
        "\t\t//printState(state, possibleUnifs, i)\n" +
        "\t\tchoosenAction := interaction.chooseNextAction(append(state.enabledActions, possibleUnifs...), &state)\n" +
        "\t\tchoosenAction.execute(&state)\n" +
        "\t\ti++\n" +
        "\t}\n" +
        "}\n" +
        "\n" +
        "func waitOffers(state *SchedState, input chan Offer) {\n" +
        "\tfor len(state.compToWait) > 0 {\n" +
        "\t\toffer := <-input\n" +
        "\t\toffer.treat(state)\n" +
        "\t\tif len(state.compToWait) != 0 {\n" +
        "\t\t\tlastElem = state.compToWait[0]\n" +
        "\t\t}\n" +
        "\t}\n" +
        "}\n" +
        "\n" +
        "//resgata o último elemento antes do deadlock que acabou de ocorrer\n" +
        "func GetLastElemBeforeDeadlock() string {\n" +
        "\treturn lastElem\n" +
        "}\n" +
        "\n" +
        "// remove the name compName from the slice components, if it is there\n" +
        "func removeCompName(compName string, components []string) []string {\n" +
        "\tfor i, c := range components {\n" +
        "\t\tif c == compName {\n" +
        "\t\t\tcomponents[i] = components[len(components)-1]\n" +
        "\t\t\treturn components[:len(components)-1]\n" +
        "\t\t}\n" +
        "\t}\n" +
        "\treturn components\n" +
        "}\n" +
        "\n" +
        "// removes all actions that are from the same component as a\n" +
        "func remObsoleteAction(a InfoAO, possAction []Action) []Action {\n" +
        "\tnb_removed := 0\n" +
        "\tfor i, c := range possAction {\n" +
        "\t\tif c.getCompName() == a.getCompName() {\n" +
        "\t\t\tpossAction[i] = possAction[len(possAction)-1]\n" +
        "\t\t\tnb_removed++\n" +
        "\t\t}\n" +
        "\t}\n" +
        "\treturn possAction[:len(possAction)-nb_removed]\n" +
        "}\n" +
        "\n" +
        "// choose between interactive and plasma execution\n" +
        "func parseArgs(args []string) InteractionMode {\n" +
        "\tif len(args) > 1 {\n" +
        "\t\tif args[1] == \"-i\" {\n" +
        "\t\t\treturn UserInterface{}\n" +
        "\t\t}\n" +
        "\t\tif args[1] == \"-d\" {\n" +
        "\t\t\treturn &DotInterface{}\n" +
        "\t\t}\n" +
        "\t}\n" +
        "\treturn &PlasmaInterface{}\n" +
        "}\n" +
        "\n" +
        "// used for debug\n" +
        "func dispArray(toDisp []string) {\n" +
        "\tfmt.Println(\"####################\")\n" +
        "\tfor i, s := range toDisp {\n" +
        "\t\tfmt.Println(\"#\", i, s)\n" +
        "\t}\n" +
        "\tfmt.Println(\"####################\")\n" +
        "}\n" +
        "\n" +
        "// also used for debug\n" +
        "func printState(state SchedState, possibleUnifs []Action, i int) {\n" +
        "\tindent := 0\n" +
        "\tdisplay(indent, \"At step \"+strconv.Itoa(i)+\", cycle \"+strconv.Itoa(current_cycle))\n" +
        "\tindent += 2\n" +
        "\tfor a, comps := range state.components {\n" +
        "\t\tdisplay(indent, \"Architecture \"+a+\":\")\n" +
        "\t\tindent += 2\n" +
        "\t\tfor _, c := range comps {\n" +
        "\t\t\tc_line := \"Component \" + c + \":\"\n" +
        "\t\t\tfor conn, port := range state.enabledPorts[c] {\n" +
        "\t\t\t\tc_line += \"\\t\" + conn\n" +
        "\t\t\t\tif port.valStr != \"\" {\n" +
        "\t\t\t\t\tc_line += \"(\" + port.valStr + \")\"\n" +
        "\t\t\t\t}\n" +
        "\t\t\t}\n" +
        "\t\t\tdisplay(indent, c_line)\n" +
        "\t\t}\n" +
        "\t\tdisplay(indent, \"Unifications\")\n" +
        "\t\tindent += 2\n" +
        "\t\tfor _, u := range state.unifications[a] {\n" +
        "\t\t\tdisplay(indent, u.Src_comp+\"\\t\"+u.Src_port+\"\\t\"+u.Rec_comp+\"\\t\"+u.Rec_port)\n" +
        "\t\t}\n" +
        "\t\tindent -= 2\n" +
        "\t\tindent -= 2\n" +
        "\t}\n" +
        "}\n" +
        "\n" +
        "func space(indent int) string {\n" +
        "\tres := \"\"\n" +
        "\tfor ; indent > 0; indent-- {\n" +
        "\t\tres += \" \"\n" +
        "\t}\n" +
        "\treturn res\n" +
        "}\n" +
        "\n" +
        "func display(indent int, msg string) {\n" +
        "\tfmt.Println(space(indent), msg)\n" +
        "}\n" +
        "\n";
    }
    public static String getSchedulerCodeWithNoPrint() {
        return
                "package scheduler\n" +
        "\n" +
        "import (\n" +
        "\t//\"fmt\"\n" +
        "\t\"strconv\"\n" +
        ")\n" +
        "\n" +
        "// indicates the current time\n" +
        "var current_cycle = 0\n" +
        "\n" +
        "var lastElem = \"\" //Guarda o último elemento antes do deadlock (se) acontecer\n" +
        "\n" +
        "// increase the time by 1\n" +
        "func Tick() {\n" +
        "\tcurrent_cycle += 1\n" +
        "}\n" +
        "\n" +
        "// Scheduler status\n" +
        "type SchedState struct {\n" +
        "\tcompToWait     []string\n" +
        "\tunifications   map[string][]Unification\n" +
        "\tcomponents     map[string][]string\n" +
        "\tenabledActions []Action\n" +
        "\tenabledPorts   map[string]map[string]Port\n" +
        "\treplyChan      map[string]chan Reply\n" +
        "\tdoneComps      []string\n" +
        "\t//offers			map[string]CompOffer\n" +
        "}\n" +
        "\n" +
        "// Schedules the whole system:\n" +
        "// - initializes the state\n" +
        "// - loop:\n" +
        "//     * receive list of possible action for each component\n" +
        "//     * declare to the interface the components that terminated (done)\n" +
        "//     * compute possible communication actions (through unifications)\n" +
        "//     * let the interface choose an action among the possible ones\n" +
        "//     * execute the choosen action\n" +
        "func Scheduler(offersinput chan Offer, main_comp string, args []string) {\n" +
        "\tinteraction := parseArgs(args)\n" +
        "\tinteraction.init()\n" +
        "\tstate := SchedState{[]string{main_comp},\n" +
        "\t\tmap[string][]Unification{},\n" +
        "\t\tmap[string][]string{},\n" +
        "\t\t[]Action{},\n" +
        "\t\tmap[string]map[string]Port{main_comp: map[string]Port{}},\n" +
        "\t\tmap[string]chan Reply{},\n" +
        "\t\t[]string{}}\n" +
        "\ti := 1\n" +
        "\tfor {\n" +
        "\t\twaitOffers(&state, offersinput)\n" +
        "\t\tinteraction.emptyDoneComps(&state)\n" +
        "\t\tpossibleUnifs := listPossibleUnifications(state)\n" +
        "\t\t//printState(state, possibleUnifs, i)\n" +
        "\t\tchoosenAction := interaction.chooseNextAction(append(state.enabledActions, possibleUnifs...), &state)\n" +
        "\t\tchoosenAction.execute(&state)\n" +
        "\t\ti++\n" +
        "\t}\n" +
        "}\n" +
        "\n" +
        "func waitOffers(state *SchedState, input chan Offer) {\n" +
        "\tfor len(state.compToWait) > 0 {\n" +
        "\t\toffer := <-input\n" +
        "\t\toffer.treat(state)\n" +
        "\t\tif len(state.compToWait) != 0 {\n" +
        "\t\t\tlastElem = state.compToWait[0]\n" +
        "\t\t}\n" +
        "\t}\n" +
        "}\n" +
        "\n" +
        "//resgata o último elemento antes do deadlock que acabou de ocorrer\n" +
        "func GetLastElemBeforeDeadlock() string {\n" +
        "\treturn lastElem\n" +
        "}\n" +
        "\n" +
        "// remove the name compName from the slice components, if it is there\n" +
        "func removeCompName(compName string, components []string) []string {\n" +
        "\tfor i, c := range components {\n" +
        "\t\tif c == compName {\n" +
        "\t\t\tcomponents[i] = components[len(components)-1]\n" +
        "\t\t\treturn components[:len(components)-1]\n" +
        "\t\t}\n" +
        "\t}\n" +
        "\treturn components\n" +
        "}\n" +
        "\n" +
        "// removes all actions that are from the same component as a\n" +
        "func remObsoleteAction(a InfoAO, possAction []Action) []Action {\n" +
        "\tnb_removed := 0\n" +
        "\tfor i, c := range possAction {\n" +
        "\t\tif c.getCompName() == a.getCompName() {\n" +
        "\t\t\tpossAction[i] = possAction[len(possAction)-1]\n" +
        "\t\t\tnb_removed++\n" +
        "\t\t}\n" +
        "\t}\n" +
        "\treturn possAction[:len(possAction)-nb_removed]\n" +
        "}\n" +
        "\n" +
        "// choose between interactive and plasma execution\n" +
        "func parseArgs(args []string) InteractionMode {\n" +
        "\tif len(args) > 1 {\n" +
        "\t\tif args[1] == \"-i\" {\n" +
        "\t\t\treturn UserInterface{}\n" +
        "\t\t}\n" +
        "\t\tif args[1] == \"-d\" {\n" +
        "\t\t\treturn &DotInterface{}\n" +
        "\t\t}\n" +
        "\t}\n" +
        "\treturn &PlasmaInterface{}\n" +
        "}\n" +
        "\n" +
        "// used for debug\n" +
        "//func dispArray(toDisp []string) {\n" +
        "\t//fmt.Println(\"####################\")\n" +
        "\t//for i, s := range toDisp {\n" +
        "\t\t//fmt.Println(\"#\", i, s)\n" +
        "//\t}\n" +
        "\t//fmt.Println(\"####################\")\n" +
        "//}\n" +
        "\n" +
        "// also used for debug\n" +
        "func printState(state SchedState, possibleUnifs []Action, i int) {\n" +
        "\tindent := 0\n" +
        "\tdisplay(indent, \"At step \"+strconv.Itoa(i)+\", cycle \"+strconv.Itoa(current_cycle))\n" +
        "\tindent += 2\n" +
        "\tfor a, comps := range state.components {\n" +
        "\t\tdisplay(indent, \"Architecture \"+a+\":\")\n" +
        "\t\tindent += 2\n" +
        "\t\tfor _, c := range comps {\n" +
        "\t\t\tc_line := \"Component \" + c + \":\"\n" +
        "\t\t\tfor conn, port := range state.enabledPorts[c] {\n" +
        "\t\t\t\tc_line += \"\\t\" + conn\n" +
        "\t\t\t\tif port.valStr != \"\" {\n" +
        "\t\t\t\t\tc_line += \"(\" + port.valStr + \")\"\n" +
        "\t\t\t\t}\n" +
        "\t\t\t}\n" +
        "\t\t\tdisplay(indent, c_line)\n" +
        "\t\t}\n" +
        "\t\tdisplay(indent, \"Unifications\")\n" +
        "\t\tindent += 2\n" +
        "\t\tfor _, u := range state.unifications[a] {\n" +
        "\t\t\tdisplay(indent, u.Src_comp+\"\\t\"+u.Src_port+\"\\t\"+u.Rec_comp+\"\\t\"+u.Rec_port)\n" +
        "\t\t}\n" +
        "\t\tindent -= 2\n" +
        "\t\tindent -= 2\n" +
        "\t}\n" +
        "}\n" +
        "\n" +
        "func space(indent int) string {\n" +
        "\tres := \"\"\n" +
        "\tfor ; indent > 0; indent-- {\n" +
        "\t\tres += \" \"\n" +
        "\t}\n" +
        "\treturn res\n" +
        "}\n" +
        "\n" +
        "func display(indent int, msg string) {\n" +
        "\t//fmt.Println(space(indent), msg)\n" +
        "}\n" +
        "\n";
    }    
}

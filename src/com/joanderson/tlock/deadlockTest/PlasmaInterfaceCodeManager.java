package com.joanderson.tlock.deadlockTest;

public class PlasmaInterfaceCodeManager {
    public static String getPlasmaInterfaceCode() {
        return
                "package scheduler\n" +
        "\n" +
        "import (\n" +
        "\t\"bufio\"\n" +
        "\tcrand \"crypto/rand\"\n" +
        "\t\"fmt\"\n" +
        "\t\"math/rand\"\n" +
        "\t\"os\"\n" +
        "\t\"strings\"\n" +
        ")\n" +
        "\n" +
        "type PlasmaInterface struct {\n" +
        "	read_lines       int\n" +
        "	command_buf_size int\n" +
        "	commands_sent    int\n" +
        "}\n" +
        "\n" +
        "var prng *rand.Rand\n" +
        "\n" +
        "func (p *PlasmaInterface) init() {\n" +
        "	fmt.Println(\"init\")\n" +
        "	initRandSeed()\n" +
        "	p.read_lines = 1\n" +
        "\tp.command_buf_size = 1000\n" +
        "	p.commands_sent = 0\n" +
        "}\n" +
        "\n" +
        "func initRandSeed() {\n" +
        "	// create a slice of size 4 and read random bytes\n" +
        "	a := make([]byte, 8)\n" +
        "	crand.Reader.Read(a)\n" +
        "	// create an int64 out of these bytes\n" +
        "	var seed, mult int64\n" +
        "	seed = 1\n" +
        "	mult = 1\n" +
        "	for _, b := range a {\n" +
        "		seed = seed + int64(b)*mult\n" +
        "		mult = mult * 256\n" +
        "	}\n" +
        "	//seed = 2 //for debug, it might be interesting to set a fixed seed\n" +
        "	prng = rand.New(rand.NewSource(seed))\n" +
        "	// prng is private to the package\n" +
        "}\n" +
        "\n" +
        "func (p *PlasmaInterface) chooseNextAction(acts []Action, state *SchedState) Action {\n" +
        "	res := randomChoice(acts)\n" +
        "	p.displayPlasma(res, state)\n" +
        "	return res\n" +
        "}\n" +
        "\n" +
        "func (p *PlasmaInterface) displayPlasma(a Action, state *SchedState) {\n" +
        "	for _, l := range a.plasmaSteps(state) {\n" +
        "		if p.commands_sent == p.command_buf_size {\n" +
        "			fmt.Println(\"dataEnd\")\n" +
        "			waitPlasma()\n" +
        "			p.commands_sent = 0\n" +
        "		}\n" +
        "		fmt.Println(current_cycle, l)\n" +
        "		p.read_lines++\n" +
        "		p.commands_sent++\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "func waitPlasma() {\n" +
        "	stdin := bufio.NewReader(os.Stdin)\n" +
        "	cmd, _ := stdin.ReadBytes('\\n')\n" +
        "	command := strings.TrimRight(string(cmd), \" \\r\\n\")\n" +
        "	if string(command) == \"quit\" {\n" +
        "		os.Exit(0)\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "// Uniformly choose an int between start and end (both included)\n" +
        "func chooseInt(start, end int) int {\n" +
        "	size := end - start + 1\n" +
        "	return prng.Intn(size) + start\n" +
        "}\n" +
        "\n" +
        "// function for randomly choosing the next action\n" +
        "func randomChoice(possibleActions []Action) Action {\n" +
        "	if len(possibleActions) == 0 {\n" +
        "		fmt.Println(\"deadlock no elemento\", GetLastElemBeforeDeadlock())\n" +
        "\n" +
        "		os.Exit(101)\n" +
        "	}\n" +
        "	i := chooseInt(0, len(possibleActions)-1)\n" +
        "	return possibleActions[i]\n" +
        "}\n" +
        "\n" +
        "// transmits to plasma the list of components to remove\n" +
        "func (p PlasmaInterface) emptyDoneComps(state *SchedState) {\n" +
        "	for _, c := range state.doneComps {\n" +
        "		fmt.Println(current_cycle, \"done \"+c)\n" +
        "	}\n" +
        "	state.doneComps = []string{}\n" +
        "}\n" +
        "\n";
    }

    public static String getPlasmaInterfaceCodeWithNoPrint() {
        return
                "package scheduler\n" +
        "\n" +
        "import (\n" +
        "\t\"bufio\"\n" +
        "\tcrand \"crypto/rand\"\n" +
        "\t\"fmt\"\n" +
        "\t\"math/rand\"\n" +
        "\t\"os\"\n" +
        "\t\"strings\"\n" +
        ")\n" +
        "\n" +
        "type PlasmaInterface struct {\n" +
        "	read_lines       int\n" +
        "	command_buf_size int\n" +
        "	commands_sent    int\n" +
        "}\n" +
        "\n" +
        "var prng *rand.Rand\n" +
        "\n" +
        "func (p *PlasmaInterface) init() {\n" +
        "//	fmt.Println(\"init\")\n" +
        "	initRandSeed()\n" +
        "	p.read_lines = 1\n" +
        "\tp.command_buf_size = 1000\n" +
        "	p.commands_sent = 0\n" +
        "}\n" +
        "\n" +
        "func initRandSeed() {\n" +
        "	// create a slice of size 4 and read random bytes\n" +
        "	a := make([]byte, 8)\n" +
        "	crand.Reader.Read(a)\n" +
        "	// create an int64 out of these bytes\n" +
        "	var seed, mult int64\n" +
        "	seed = 1\n" +
        "	mult = 1\n" +
        "	for _, b := range a {\n" +
        "		seed = seed + int64(b)*mult\n" +
        "		mult = mult * 256\n" +
        "	}\n" +
        "	//seed = 2 //for debug, it might be interesting to set a fixed seed\n" +
        "	prng = rand.New(rand.NewSource(seed))\n" +
        "	// prng is private to the package\n" +
        "}\n" +
        "\n" +
        "func (p *PlasmaInterface) chooseNextAction(acts []Action, state *SchedState) Action {\n" +
        "	res := randomChoice(acts)\n" +
        "	p.displayPlasma(res, state)\n" +
        "	return res\n" +
        "}\n" +
        "\n" +
        "func (p *PlasmaInterface) displayPlasma(a Action, state *SchedState) {\n" +
        "	for _, l := range a.plasmaSteps(state) {\n" +
        "		if p.commands_sent == p.command_buf_size {\n" +
        "			fmt.Println(\"Nenhum deadlock identificado no programa\")\n" +
        "           os.Exit(0)\n" +
        "			p.commands_sent = 0\n" +
        "		}\n" +
        "		fmt.Println(current_cycle, l)\n" +
        "		p.read_lines++\n" +
        "		p.commands_sent++\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "func waitPlasma() {\n" +
        "	stdin := bufio.NewReader(os.Stdin)\n" +
        "	cmd, _ := stdin.ReadBytes('\\n')\n" +
        "	command := strings.TrimRight(string(cmd), \" \\r\\n\")\n" +
        "	if string(command) == \"quit\" {\n" +
        "		os.Exit(0)\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "// Uniformly choose an int between start and end (both included)\n" +
        "func chooseInt(start, end int) int {\n" +
        "	size := end - start + 1\n" +
        "	return prng.Intn(size) + start\n" +
        "}\n" +
        "\n" +
        "// function for randomly choosing the next action\n" +
        "func randomChoice(possibleActions []Action) Action {\n" +
        "	if len(possibleActions) == 0 {\n" +
        "		fmt.Println(\"deadlock no elemento\", GetLastElemBeforeDeadlock())\n" +
        "\n" +
        "		os.Exit(101)\n" +
        "	}\n" +
        "	i := chooseInt(0, len(possibleActions)-1)\n" +
        "	return possibleActions[i]\n" +
        "}\n" +
        "\n" +
        "// transmits to plasma the list of components to remove\n" +
        "func (p PlasmaInterface) emptyDoneComps(state *SchedState) {\n" +
        "	for _, c := range state.doneComps {\n" +
        "		_ = c\n" +
        "	}\n" +
        "	state.doneComps = []string{}\n" +
        "}\n" +
        "\n";
    }
    
}

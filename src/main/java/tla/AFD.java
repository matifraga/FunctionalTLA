package tla;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class AFD extends AF{
	
	public AFD(final SortedSet<String> alp, final SortedSet<String> st, final Set<String> fst, final Set<String>[][] table, final String init) {
		if (table.length != st.size() || table[0].length != alp.size())
			throw new IllegalArgumentException("Delta dimensions are wrong (#Rows != #States or #Columns != #Alphabet");
		if (!st.containsAll(fst) || !st.contains(init))
			throw new IllegalArgumentException("All final and initial states must be included in States");
		
		this.setAlphabet(alp)
			.setStates(st)
			.setFinalStates(fst)
			.setDeltas(table)
			.setInitialState(init);
	}
	public AFD(final SortedSet<String> alp, final SortedSet<String> st, final Set<String> fst, final String init) {
		@SuppressWarnings("unchecked")
		Set<String>[][] delta = new HashSet[st.size()][alp.size()];
		for (int i = 0; i < delta.length; i++) {
			for (int j = 0; j < delta[i].length; j++) {
				delta[i][j] = new HashSet<>();
			}
		}
		if (!st.containsAll(fst) || !st.contains(init))
			throw new IllegalArgumentException("All final and initial states must be included in States");
		
		this.setAlphabet(alp)
		.setStates(st)
		.setFinalStates(fst)
		.setDeltas(delta)
		.setInitialState(init);
	}
	
	public GR toGR() {
		Set<String> terminals = new HashSet<>();
		alphabet.forEach(c -> terminals.add(c));
		Set<String> nonTerminals = new HashSet<>();
		nonTerminals.addAll(states);
		String initialState = this.initialState;
		Map<String, Set<String>> predicates = new HashMap<String, Set<String>>();
		
		for(String st : states) {
			for(String c: alphabet) {
				String r = getDelta(st, c);
				if(r!=null) {
					Set<String> to = predicates.getOrDefault(st, new HashSet<>());
					to.add(c.concat(" ").concat(r));
					predicates.put(st,to);
					nonTerminals.add(st);
					nonTerminals.add(r);
					if(finalStates.contains(r)) {
						Set<String> aux = predicates.getOrDefault(st, new HashSet<>());
						aux.add(c);
						predicates.put(st,aux);
					}
				}
			}
		}
		if(finalStates.contains(initialState)) {
			Set<String> aux = predicates.getOrDefault(initialState, new HashSet<>());
			aux.add(String.valueOf("\\"));
			predicates.put(initialState,aux);
		}		
		return new GR(nonTerminals, terminals, predicates, initialState);
	}

	public String getDelta(String state, String character) {
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		int j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if(j==-1 || i==-1)
			return null;
		return deltas[i][j].stream().findFirst().orElse(null);
	}
	
	public boolean setDelta(String state, String character, String delta){
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		int j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if(delta != null) {
			if(j==-1 || i==-1 || !states.contains(delta))
				return Boolean.FALSE;
		}
		Set<String> set = new HashSet<>();
		if(delta != null)
			set.add(delta);
		deltas[i][j] = set;
		return Boolean.TRUE;
	}

	@Override
	public AFD toAFD() {
		return this;
	}
	@Override
	public AFND toAFND() {
		return new AFND(getAlphabet(), getStates(), getFinalStates(), getDeltas(), getInitialState());
	}
	
	@Override
	public AFNDL toAFNDL() {
		AFNDL ans = new AFNDL(getAlphabet(), getStates(), getFinalStates(), getInitialState());
		for (String st : getStates()) {
			for (String c : getAlphabet()) {
				Set<String> thisSet = new HashSet<String>();
				String thisDelta = getDelta(st, c);
				if (thisDelta != null) {
					thisSet.add(thisDelta);
					ans.setDelta(st, c, thisSet);	
				}
			}
		}
		return ans;
	}
}

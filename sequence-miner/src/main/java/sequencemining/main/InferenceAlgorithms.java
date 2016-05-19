package sequencemining.main;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Map;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;

import sequencemining.sequence.Sequence;
import sequencemining.transaction.Transaction;

/** Container class for Inference Algorithms */
public class InferenceAlgorithms {

	/** Interface for the different inference algorithms */
	public interface InferenceAlgorithm {
		public Multiset<Sequence> infer(final Transaction transaction);
	}

	/**
	 * Infer ML parameters to explain transaction using greedy algorithm and
	 * store in covering. Sequences *may not* overlap.
	 * <p>
	 * !! Assumes *no overlap* !! i.e. subseqs in covering are pairwise disjoint
	 */
	public static class InferGreedy implements InferenceAlgorithm, Serializable {
		private static final long serialVersionUID = 9173178089235828142L;

		@Override
		public Multiset<Sequence> infer(final Transaction transaction) {

			final Multiset<Sequence> covering = HashMultiset.create();
			int lenCovering = 0;
			final int transactionSize = transaction.size();
			final BitSet coveredItems = new BitSet(transactionSize);

			final Table<Sequence, Integer, Double> cachedSequences = transaction.getCachedSequences();
			while (coveredItems.cardinality() != transactionSize) {

				double minCostPerItem = Double.POSITIVE_INFINITY;
				Sequence bestSeq = null;
				BitSet bestSeqCoveredItems = null;

				for (final Sequence seq : cachedSequences.rowKeySet()) {

					// How many additional items does sequence cover?
					final BitSet seqCoveredItems = transaction.getCovered(seq, coveredItems);
					// Ignore sequences which don't cover anything
					if (seqCoveredItems.isEmpty())
						continue;

					// Get seq multiplicity in covering
					final int occur = covering.count(seq);

					// TODO triple check that this is right!!!
					// Calculate f(CuS) - f(C)
					Double prob1 = cachedSequences.get(seq, occur + 1);
					if (prob1 == null)
						prob1 = 0.; // Empty multiplicities have zero prob
					else if (prob1 == 0. && isInnerProb(occur + 1, cachedSequences.row(seq)))
						prob1 = Double.MIN_VALUE; // Smooth zero inner probs
					double prob = cachedSequences.get(seq, occur);
					if (prob == 0. && isInnerProb(occur, cachedSequences.row(seq)))
						prob = Double.MIN_VALUE; // Smooth zero inner probs
					final double cost = -Math.log(prob1) + Math.log(prob)
							+ sumLogRange(lenCovering + 1, lenCovering + seq.size());
					final double costPerItem = cost / seq.size();

					if (costPerItem < minCostPerItem) {
						minCostPerItem = costPerItem;
						bestSeq = seq;
						bestSeqCoveredItems = seqCoveredItems;
					}

				}

				if (bestSeq != null) {
					// final int firstItemCovered = bestSeqCoveredItems
					// .nextSetBit(0);
					// covering.put(bestSeq, firstItemCovered);
					covering.add(bestSeq);
					lenCovering += bestSeq.size();
					coveredItems.or(bestSeqCoveredItems);
				} else { // Fill in incomplete coverings with singletons
					int index = 0;
					while (coveredItems.cardinality() != transactionSize) {
						index = coveredItems.nextClearBit(index);
						final Sequence seq = new Sequence(transaction.get(index));
						covering.add(seq);
						coveredItems.set(index);
					}
					return covering;
				}

			}
			return covering;
		}

		private boolean isInnerProb(final int probIndex, final Map<Integer, Double> probVec) {
			for (int i = probIndex + 1; i < probVec.size(); i++) {
				if (probVec.get(i) != 0.)
					return true;
			}
			return false;
		}

		private double sumLogRange(final int a, final int b) {
			double sum = 0;
			for (int i = a; i <= b; i++)
				sum += Math.log(i);
			return sum;
		}

	}

	// /**
	// * Infer ML parameters to explain transaction using greedy algorithm and
	// * store in covering. Sequences may overlap.
	// * <p>
	// * This is an O(log(n))-approximation algorithm where n is the number of
	// * elements in the transaction.
	// */
	// public static class InferGreedyOld implements InferenceAlgorithm,
	// Serializable {
	// private static final long serialVersionUID = 9173178089235828142L;
	//
	// @Override
	// public HashSet<Sequence> infer(final Transaction transaction) {
	//
	// final HashSet<Sequence> covering = new HashSet<>();
	// final int transactionSize = transaction.size();
	// final BitSet coveredItems = new BitSet(transactionSize);
	//
	// final HashMap<Sequence, Double> cachedSequences = transaction
	// .getCachedSequences();
	// while (coveredItems.cardinality() != transactionSize) {
	//
	// double minCostPerItem = Double.POSITIVE_INFINITY;
	// Sequence bestSeq = null;
	// BitSet bestSeqCoveredItems = null;
	//
	// for (final Entry<Sequence, Double> entry : cachedSequences
	// .entrySet()) {
	//
	// // Ignore sequences which already cover
	// if (covering.contains(entry.getKey()))
	// continue;
	//
	// // How many additional items does sequence cover?
	// final BitSet seqCoveredItems = transaction.getCovered(
	// entry.getKey(), coveredItems);
	// // Ignore sequences which don't cover anything
	// if (seqCoveredItems.isEmpty())
	// continue;
	// final BitSet newlyCoveredItems = (BitSet) seqCoveredItems
	// .clone();
	// newlyCoveredItems.or(coveredItems);
	// final int notCovered = newlyCoveredItems.cardinality()
	// - coveredItems.cardinality();
	//
	// final double cost = -Math.log(entry.getValue());
	// final double costPerItem = cost / notCovered;
	//
	// if (costPerItem < minCostPerItem) {
	// minCostPerItem = costPerItem;
	// bestSeq = entry.getKey();
	// bestSeqCoveredItems = seqCoveredItems;
	// }
	//
	// }
	//
	// if (bestSeq != null) {
	// // final int firstItemCovered = bestSeqCoveredItems
	// // .nextSetBit(0);
	// // covering.put(bestSeq, firstItemCovered);
	// covering.add(bestSeq);
	// coveredItems.or(bestSeqCoveredItems);
	// } else { // Allow incomplete coverings
	// break;
	// }
	//
	// }
	// return covering;
	// }
	//
	// }

	private InferenceAlgorithms() {

	}

}

package nz.ac.squash.db.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;

@Entity
@Table(name = "match_result")
public class MatchResult {
    @Id
    @GeneratedValue
    protected long mID;

    private Date mDate;

    @ManyToOne
    private Member mWinner;

    @ManyToOne
    private Member mLoser;

    @ManyToOne
    private Match mMatch = null;

    public MatchResult() {
        mDate = new Date();
    }

    public MatchResult(Member winner, Match match) {
        this();

        mWinner = winner;
        mLoser = match.getPlayer1().equals(winner) ? match.getPlayer2() : match
                .getPlayer1();

        mMatch = match;
    }

    public MatchResult(Member winner, Member loser) {
        this();

        mWinner = winner;
        mLoser = loser;

        mMatch = null;
    }

    public long getID() {
        return mID;
    }

    public Date getDate() {
        return mDate;
    }

    public Member getWinner() {
        return mWinner;
    }

    public Member getLoser() {
        return mLoser;
    }

    public Match getMatch() {
        return mMatch;
    }

    @Override
    public int hashCode() {
        return (int) (mID ^ (mID >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MatchResult)) return false;

        return ((MatchResult) obj).mID == mID;
    }

    @Override
    public String toString() {
        return mWinner.getNameFormatted() + " beat " + mLoser != null ? mLoser
                .getNameFormatted() : "noone";
    }

    // Adds a user to the ladder if they have not been already.
    public static void addToLadder(final Member member) {
        DB.queueTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                // Abort if already on ladder.
                if (!query(MatchResult.class, "r where mWinner_mID = ?0",
                        member).isEmpty()) return;

                update(new MatchResult(member, (Member) null));
            }
        });
    }

    public static List<Member> getLadder() {
        final List<Member> ladder = new ArrayList<Member>();

        DB.executeTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                for (MatchResult result : listAll(MatchResult.class)) {
                    int winnerPos = ladder.size();
                    int loserPos = ladder.size();

                    for (int i = 0; i < ladder.size(); ++i) {
                        if (ladder.get(i).equals(result.getWinner())) {
                            winnerPos = i;
                            break;
                        } else if (ladder.get(i).equals(result.getLoser())) {
                            loserPos = i;
                            break;
                        }
                    }

                    ladder.remove(result.getWinner());
                    ladder.add(Math.min(winnerPos, loserPos),
                            result.getWinner());
                }
            }
        });

        return ladder;
    }
}
package eaf.cm;

import java.util.Date;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeSet;

import java.util.Set;

import eaf.core.common.EafCommon;
import eaf.core.common.EafException;
import eaf.core.common.StaticVars;
import eaf.core.common.dao.EaDAO;
import eaf.core.common.dao.GeneralDAO;
import eaf.core.entities.ea.BusinessEntity;

public class CmHierarch {

	static Map<Integer, Object> orgsCM = new HashMap<Integer, Object>();

	static Map<Integer, int[]> beCM = new HashMap<Integer, int[]>();

	static Connection c = null;

	static EaDAO eaDAO;

	static Calendar createTime;

	/**
	 * beIdsInOrg. (recursive method not used.)
	 *
	 * @param incOrgId
	 *
	 * @return csn list of business entity ids found in an Org.
	 *
	 */
	public String beIdsInOrg(final String incOrgId[]) {

		String be_list = "";

		Set<Integer> be_set = new HashSet<Integer>();

		for (int index = 0; index < incOrgId.length; index++) {

			HashMap<Integer, int[]> beHash = (HashMap<Integer, int[]>) orgsCM
					.get(Integer.parseInt(incOrgId[index]));

			for (Integer beKey : beHash.keySet()) {

				be_set.add(beKey);

				for (int child : beHash.get(beKey))
					be_set.add(child);

			}

			for (Object be : be_set.toArray()) {

				if (!be_list.equals(""))
					be_list += ",";

				be_list += be;

			}
		}

		return be_list;
	}

	/**
	 * beIdsInOrgHQL.
	 *
	 * @param incOrgId
	 *
	 * @return String csv list of business entity ids found in an Org.
	 *
	 */
	public static String beIdsInOrgHQL(final String incOrgId[]) {

		String be_list = "0";

		Set<String> be_set = new TreeSet<String>();

		ArrayList<?> olist;

		EaDAO eaDao = (EaDAO) GeneralDAO
				.get(eaf.core.entities.ea.BusinessEntity.class);

		for (int index = 0; index < incOrgId.length; index++) {

			olist = eaDao
					.executeTrustedHQL("select s.id.beList from ViewOrgBeList s where s.id.orgId ="
							+ incOrgId[index]);

			try {
				if (!olist.get(0).toString().trim().equals(""))
					be_set.addAll(Arrays.asList(olist.get(0).toString().trim()
							.split(",")));
			} catch (Exception e) {
				new EafException("", e);
			}
		}

		for (String be : be_set) {

			if (be_list != "")
				be_list += ",";

			be_list += be;

		}

		be_list += ", " + beOrphansHQL();
		
		return be_list;
	}

	/**
	 * beIdsInDirectorateHQL.
	 *
	 * @param incOrgId
	 *
	 * @return String csv list of business entity ids found in an Org.
	 *
	 */
	public static String beIdsInDirectorateHQL(final String[] incDirId) {

		String be_list = "";

		Set<Integer> be_set = new TreeSet<Integer>();

		ArrayList<?> olist;

		EaDAO eaDao = (EaDAO) GeneralDAO
				.get(eaf.core.entities.ea.BusinessEntity.class);

		for (int index = 0; index < incDirId.length; index++) {

			olist = eaDao
					.executeTrustedHQL("select s.beId from BeDirAssoc s where s.archive = 0 and s.maTyCd ="
							+ incDirId[index]);

			for (Object sys_id : olist) {
				try {

					BusinessEntity sys = new BusinessEntity();
					sys.load("beId", (Integer) sys_id);

					if (sys.getArchive() == 0) {

						be_set.add(sys.getId());

						for (BusinessEntity parent : sys.getParents()) {
							be_set.add(parent.getId());
						}

					}
				} catch (Exception e) {
					new EafException("", e);
				}
			}

		}

		for (Integer be : be_set) {

			if (be_list != "")
				be_list += ",";

			be_list += be.toString();

		}

		return be_list;
	}

	/**
	 * beIdsInInfoSys. 
	 *
	 * @param incBeId
	 *
	 * @return String csv list of business entity ids found in an InfoSys.
	 *
	 */
	public static String beIdsInInfoSys(final String incBeId) {

		String be_list = "0";

		Set<String> be_set = new TreeSet<String>();

		StringTokenizer tokens;

		List olist = null;

		org.hibernate.Session sess = EafCommon.getDBSession(0);

		org.hibernate.Transaction trans = sess.beginTransaction();

		olist = sess.createSQLQuery(
				"select tmap from ea.recurse_down(" + incBeId + ")").list();

		trans.commit();

		sess.close();

		for (Object tmap : olist) {
			tokens = new StringTokenizer((String) tmap,
					StaticVars.EAF_DELIMITER);
			while (tokens.hasMoreTokens())
				be_set.add(tokens.nextToken());
		}

		for (String be : be_set) {

			if (be_list != "")
				be_list += ",";

			be_list += be;

		}

		return be_list;
	}

	/**
	 * beOrphansHQL.
	 *
	 * @param incOrgId
	 *
	 * @return String csv list of hardware not assigned to a parent workstation.
	 *
	 */
	public static String beOrphansHQL() {

		String be_list = "0";
		
		String orphan_list = "0";

		Set<String> be_set = new TreeSet<String>();

		ArrayList<?> olist;

		EaDAO eaDao = (EaDAO) GeneralDAO
				.get(eaf.core.entities.ea.BusinessEntity.class);

	
		olist = eaDao
				.executeTrustedHQL("select s.id.beList from ViewOrgBeList s ");


		for (int index = 0; index < olist.size(); index++) {
			
			try {
				if (!olist.get(index).toString().trim().equals(""))
					be_set.addAll(Arrays.asList(olist.get(index).toString().trim()
							.split(",")));
			} catch (Exception e) {
				new EafException("", e);
			}
		}

		for (String be : be_set) {

			if (be_list != "")
				be_list += ",";

			be_list += be;

		}

		
		olist = eaDao
		   .executeTrustedHQL("select s.beId from BusinessEntity s where s.beId not in (" + be_list + ") and "
				   + " s.beId not in (select t.beId from BeOrgAssoc t) and s.archive = 0");

		
		for (Object obe : olist) {

			if (orphan_list != "")
				orphan_list += ",";

			orphan_list += obe;

		}

		return orphan_list;
		
	}
	
	/**
	 * cmOrphansHQL.
	 *
	 * @param incOrgId
	 *
	 * @return String csv list of  workstation not attached to a parent business entity,
	 * hardware not attached to a workstation, and software not attached to parent hardware
	 *
	 */
	public static String cmOrphansHQL(final String cmType) {

//		String cm_list = "0";
//		
		String orphan_list = "0";
//
//		Set<String> be_set = new TreeSet<String>();
//
//		ArrayList<?> olist;
//
//		EaDAO eaDao = (EaDAO) GeneralDAO
//				.get(eaf.core.entities.ea.BusinessEntity.class);
//
//	
//      if (cmType.equals("Workstation")
//		   sql = 
//      else if (cmType.equals("Hardware")
//		   sql = 
//      else (cmType.equals("Software")
//		   sql = 
//		
//		
//		olist = eaDao
//				.executeTrustedHQL(sql);
//
//
//		for (int index = 0; index < olist.size(); index++) {
//			
//			try {
//				if (!olist.get(index).toString().trim().equals(""))
//					be_set.addAll(Arrays.asList(olist.get(index).toString().trim()
//							.split(",")));
//			} catch (Exception e) {
//				new EafException("", e);
//			}
//		}
//
//		for (String be : be_set) {
//
//			if (be_list != "")
//				be_list += ",";
//
//			be_list += be;
//
//		}
//
//		
//		olist = eaDao
//		   .executeTrustedHQL("select s.beId from BusinessEntity s where s.beId not in (" + be_list + ") and "
//				   + " s.beId not in (select t.beId from BeOrgAssoc t) and s.archive = 0");
//
//		
//		for (Object obe : olist) {
//
//			if (orphan_list != "")
//				orphan_list += ",";
//
//			orphan_list += obe;
//
//		}
//
		return orphan_list;
//		
	}
	
	
	/**
	 * init. (recursive method not used.) 
	 *
	 */
	public static void init() {

		eaDAO = (EaDAO) GeneralDAO
				.get(eaf.core.entities.ea.BusinessEntity.class);

		if (orgsCM.keySet().size() == 0 || isStale()) {

			try {

				String hql = "select distinct b.orgId from BeOrgAssoc b where b.orgId in (select s.orgId from Org s where s.archive = 0)";

				ArrayList<?> orgs = eaDAO
						.executeTrustedHQL(hql + " order by 1");

				for (int x = 0; x < orgs.size(); x++)
					refresh((Integer) orgs.get(x), 0);

			} catch (Exception e) {
				new EafException("", e);
			}

			createTime = Calendar.getInstance();

		}

	}

	/**
	 * refresh. (recursive method not used.)
	 *
	 * @param org_id
	 *
	 * @param be_id
	 */
	public static void refresh(final Integer org_id, final int be_id) {

		ArrayList<?> r = null, r2 = null, r3 = null, parent = null, child = null;

		String sql;

		try {

			if (orgsCM.get(org_id) == null) {

				orgsCM.put(org_id, new HashMap<Integer, int[]>());

				sql = "select distinct s.beId from BeOrgAssoc s where s.archive = 0 and  s.orgId = "
						+ org_id;

				r = eaDAO.executeTrustedHQL(sql);

				for (int g = 0; g < r.size(); g++) {

					sql = "select distinct s.subBeId from BeAssoc s where s.archive = 0 and s.ordBeId = "
							+ r.get(g).toString();

					parent = eaDAO.executeTrustedHQL(sql);

					int[] p = new int[parent.size()];

					for (int h = 0; h < parent.size(); h++) {

						p[h] = Integer.parseInt(parent.get(h).toString());
					}

					((HashMap<Integer, int[]>) orgsCM.get(org_id)).put(
							(Integer) r.get(g), p);

					refresh(org_id, (Integer) r.get(g));

				}
			}

			if (!((HashMap<Integer, int[]>) orgsCM.get(org_id)).keySet()
					.contains(be_id)) {

				sql = "select distinct s.subBeId from BeAssoc s where s.archive = 0 and s.ordBeId = "
						+ be_id;

				r2 = eaDAO.executeTrustedHQL(sql);

				int[] tmp = new int[r2.size()];

				for (int i = 0; i < r2.size(); i++) {
					tmp[i] = (Integer) r2.get(i);
				}
				((HashMap<Integer, int[]>) orgsCM.get(org_id)).put(be_id, tmp);
			}

			sql = "select distinct s.subBeId from BeAssoc s where s.archive = 0 and s.ordBeId = "
					+ be_id;

			child = eaDAO.executeTrustedHQL(sql);

			for (int j = 0; j < child.size(); j++) {
				refresh(org_id, (Integer) child.get(j));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * isStale. (recursive method not used.)
	 *
	 * @return if database has bee updated since the creation of the CM Hierarch
	 *         HashMap.
	 *
	 */
	public static Boolean isStale() {

		Boolean state = false;

		ArrayList<?> alist = eaDAO
				.executeTrustedHQL("select max(s.datetime) from BeAssoc s");
		ArrayList<?> blist = eaDAO
				.executeTrustedHQL("select max(s.datetime) from BeOrgAssoc s");

		Calendar db_timestamp_a = Calendar.getInstance();
		db_timestamp_a.setTime((Date) alist.get(0));

		Calendar db_timestamp_b = Calendar.getInstance();
		db_timestamp_b.setTime((Date) blist.get(0));

		if (createTime.before(db_timestamp_a)
				|| createTime.before(db_timestamp_b)) {

			for (Integer org_id : orgsCM.keySet()) {
				((HashMap<Integer, int[]>) orgsCM.get(org_id)).clear();
			}

			orgsCM.clear();

			state = true;

		}

		return state;

	}
}

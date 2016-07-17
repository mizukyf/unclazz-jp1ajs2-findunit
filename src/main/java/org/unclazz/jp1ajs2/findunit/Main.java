package org.unclazz.jp1ajs2.findunit;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.unclazz.jp1ajs2.unitdef.Unit;
import org.unclazz.jp1ajs2.unitdef.Units;
import org.unclazz.jp1ajs2.unitdef.query.UnitIterableQuery;
import org.unclazz.jp1ajs2.unitdef.query.UnitQueries;
import org.unclazz.jp1ajs2.unitdef.util.Formatter;
import org.unclazz.jp1ajs2.unitdef.util.Formatter.FormatOptions;

public final class Main {

	public static void main(String[] args) {
		try {
			new Main().execute(args);
		} catch (final Exception e) {
			printUsage();
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void execute(final String[] args) {
		// コマンドライン引数からパラメータを読み取る
		final Parameters ps = parseArguments(args);
		// フォーマッターを初期化
		final Function<Unit, CharSequence> f = makeFormatter(ps);
		// クエリを構築
		final UnitIterableQuery q = buildQuery(ps);
		// ユニット定義を読み取る
		final List<Unit> us = Units.fromFile(ps.getSourceFile(), Charset.forName("Shift_JIS"));
		// ルートユニットごとに処理
		for (final Unit u : us) {
			// クエリを使って条件にマッチするユニットを取得
			for (final Unit u2 : u.query(q)) {
				// 指定されたフォーマットで出力
				printResult(f.apply(u2));
			}
		}
	}
	
	private static void printUsage() {
		System.out.println("USAGE: jp1ajs2.findunit -s <source>"
				+ " [ -n <unit-name-pattern>]"
				+ " [ -p <param-name>[=<param-value-pattern>]]"
				+ " [ -f {FQN_LIST|UNIT_DEF|PRITTY_PRINT}]");
	}
	
	private Function<Unit, CharSequence> makeFormatter(final Parameters ps) {
		if (ps.getOutputFormat() == OutputFormat.PRITTY_PRINT) {
			return (u) -> String.format("/* %s */%s", u.getFullQualifiedName(),
					System.lineSeparator()) + Formatter.DEFAULT.format(u);
		} else if (ps.getOutputFormat() == OutputFormat.UNIT_LIST) {
			final FormatOptions os = new FormatOptions();
			os.setLineSeparator("");
			os.setTabWidth(0);
			os.setUseSpacesForTabs(true);
			return new Formatter(os)::format;
		} else {
			return (u) -> u.getFullQualifiedName().toString();
		}
	}
	
	private void printResult(final Object o) {
		System.out.println(o);
	}
	
	private UnitIterableQuery buildQuery(final Parameters ps) {
		UnitIterableQuery q = UnitQueries.itSelfAndDescendants();
		if (ps.getUnitNamePattern() != null) {
			q = q.nameContains(ps.getUnitNamePattern());
		}
		if (ps.getParamName() != null) {
			final String pn = ps.getParamName();
			final List<String> pvs = ps.getParamValuePatterns();
			
			if (pvs.isEmpty()) {
				q = q.hasParameter(pn).anyValue();
			}
			for (int i = 0; i < pvs.size(); i ++) {
				final String pv = pvs.get(i);
				if (pv.isEmpty()) {
					q = q.hasParameter(pn).valueAt(i).anyValue();
				} else {
					q = q.hasParameter(pn).valueAt(i).contains(pv);
				}
			}
		}
		return q;
	}
	
	private Parameters parseArguments(final String[] args) {
		// コマンドライン引数からイテレータを作成
		final Iterator<String> argIter = Arrays.asList(args).iterator();
		// パラメータを格納するオブジェクトを初期化
		final Parameters ps = new Parameters();
		ps.setOutputFormat(OutputFormat.FQN_LIST);
		// 引数を1つずつループ処理
		while (argIter.hasNext()) {
			// まず引数名に該当する要素を取得
			final String argName = argIter.next();
			// 妥当な引数名かチェック
			if (!checkIfValidArgName(argName)) {
				continue;
			}
			// 「次の要素」があるかチェック
			if (!argIter.hasNext()) {
				throw argError("invalid argument sequence.");
			}
			// 引数値に該当する要素を取得
			final String argValue = argIter.next();
			// 引数名ごとに条件分岐
			if (argName.equals("-s")) {
				// 読み取り元ファイルの場合
				ps.setSourceFile(new File(argValue));
			} else if (argName.equals("-n")) {
				// ユニット名パターンの場合
				ps.setUnitNamePattern(argValue);
			} else if (argName.equals("-p")) {
				// パラメータ・パターンの場合
				// 「=」が登場する位置を検索
				final int equalSignPosition = argValue.indexOf("=");
				if (equalSignPosition == -1) {
					// 「=」が見つからなかった場合はパラメータ名のみの条件付け
					ps.setParamName(argValue);
					ps.setParamValuePatterns(Collections.<String>emptyList());
				} else {
					// 「=」が見つかった場合はパラメータ値のパターンを含む条件付け
					final String paramName = argValue.substring(0, equalSignPosition);
					final String[] paramValues = argValue.substring(equalSignPosition + 1).split(",");
					ps.setParamName(paramName);
					ps.setParamValuePatterns(Arrays.asList(paramValues));
				}
			} else if (argName.equals("-f")) {
				// フォーマット指定の場合
				ps.setOutputFormat(Arrays.stream(OutputFormat.values()).
						filter(f -> f.name().equals(argValue)).findAny()
						.orElse(OutputFormat.FQN_LIST));
			}
		}
		// パラメータが妥当かどうかをチェック
		if (checkIfValidParams(ps)) {
			return ps;
		}
		throw argError("argument is not enough.");
	}
	
	private IllegalArgumentException argError(final String message) {
		return new IllegalArgumentException(message);
	}
	
	private boolean checkIfValidArgName(final String target) {
		return Arrays.stream(new String[] {"-s", "-n", "-p", "-f"})
				.anyMatch(s -> s.equals(target));
	}
	
	private boolean checkIfValidParams(final Parameters ps) {
		if (!checkIfAllNotNull(ps.getSourceFile(), ps.getOutputFormat())) {
			return false;
		}
		if (!checkIfAnyNotNull(ps.getParamName(), ps.getUnitNamePattern())) {
			return false;
		}
		if (!ps.getSourceFile().isFile()) {
			return false;
		}
		return true;
	}
	
	private boolean checkIfAllNotNull(final Object... targets) {
		return Arrays.stream(targets).allMatch(t -> t != null);
	}
	
	private boolean checkIfAnyNotNull(final Object... targets) {
		return Arrays.stream(targets).anyMatch(t -> t != null);
	}
}

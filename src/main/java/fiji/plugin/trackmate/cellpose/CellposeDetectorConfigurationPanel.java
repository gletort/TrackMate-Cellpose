/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.cellpose;

import static fiji.plugin.trackmate.cellpose.CellposeDetectorFactory.KEY_CELLPOSE_CUSTOM_MODEL_FILEPATH;
import static fiji.plugin.trackmate.cellpose.CellposeDetectorFactory.KEY_CELLPOSE_MODEL;
import static fiji.plugin.trackmate.cellpose.CellposeDetectorFactory.KEY_CELLPOSE_PYTHON_FILEPATH;
import static fiji.plugin.trackmate.cellpose.CellposeDetectorFactory.KEY_CELL_DIAMETER;
import static fiji.plugin.trackmate.cellpose.CellposeDetectorFactory.KEY_LOGGER;
import static fiji.plugin.trackmate.cellpose.CellposeDetectorFactory.KEY_OPTIONAL_CHANNEL_2;
import static fiji.plugin.trackmate.cellpose.CellposeDetectorFactory.KEY_USE_GPU;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS;
import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.gui.Icons.PREVIEW_ICON;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.cellpose.CellposeSettings.PretrainedModel;
import fiji.plugin.trackmate.detection.DetectionUtils;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;
import fiji.plugin.trackmate.util.JLabelLogger;

public class CellposeDetectorConfigurationPanel extends ConfigurationPanel
{

	private static final long serialVersionUID = 1L;

	private static final String TITLE = CellposeDetectorFactory.NAME;

	private static final ImageIcon ICON = CellposeUtils.logo64();

	private static final NumberFormat DIAMETER_FORMAT = new DecimalFormat( "#.#" );

	protected static final String DOC1_URL = "https://imagej.net/plugins/trackmate/trackmate-cellpose";

	private final JButton btnBrowseCellposePath;

	private final JTextField tfCellposeExecutable;

	private final JComboBox< PretrainedModel > cmbboxPretrainedModel;

	private final JComboBox< String > cmbboxCh1;

	private final JComboBox< String > cmbboxCh2;

	private final JFormattedTextField ftfDiameter;

	private final JCheckBox chckbxSimplify;

	private final Logger logger;

	private final JCheckBox chckbxUseGPU;

	private final JTextField tfCustomPath;

	private final JButton btnBrowseCustomModel;

	public CellposeDetectorConfigurationPanel( final Settings settings, final Model model )
	{
		this.logger = model.getLogger();

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		gridBagLayout.columnWidths = new int[] { 144, 0, 32 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 0.0 };
		setLayout( gridBagLayout );

		final JLabel lblSettingsForDetector = new JLabel( "Settings for detector:" );
		lblSettingsForDetector.setFont( FONT );
		final GridBagConstraints gbcLblSettingsForDetector = new GridBagConstraints();
		gbcLblSettingsForDetector.gridwidth = 3;
		gbcLblSettingsForDetector.insets = new Insets( 5, 5, 5, 5 );
		gbcLblSettingsForDetector.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSettingsForDetector.gridx = 0;
		gbcLblSettingsForDetector.gridy = 0;
		add( lblSettingsForDetector, gbcLblSettingsForDetector );

		final JLabel lblDetector = new JLabel( TITLE, ICON, JLabel.RIGHT );
		lblDetector.setFont( BIG_FONT );
		lblDetector.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblDetector = new GridBagConstraints();
		gbcLblDetector.gridwidth = 3;
		gbcLblDetector.insets = new Insets( 0, 5, 5, 5 );
		gbcLblDetector.fill = GridBagConstraints.HORIZONTAL;
		gbcLblDetector.gridx = 0;
		gbcLblDetector.gridy = 1;
		add( lblDetector, gbcLblDetector );

		/*
		 * Help text.
		 */
		final JLabel lblHelptext = new JLabel( "This detector relies on Cellpose to detect objects in the image." );
		lblHelptext.setFont( FONT.deriveFont( Font.ITALIC ) );
		final GridBagConstraints gbcLblHelptext = new GridBagConstraints();
		gbcLblHelptext.anchor = GridBagConstraints.NORTH;
		gbcLblHelptext.fill = GridBagConstraints.HORIZONTAL;
		gbcLblHelptext.gridwidth = 3;
		gbcLblHelptext.insets = new Insets( 5, 10, 5, 15 );
		gbcLblHelptext.gridx = 0;
		gbcLblHelptext.gridy = 2;
		add( lblHelptext, gbcLblHelptext );

		final String text = "Click here for the documentation";
		final JLabel lblUrl = new JLabel( text );
		lblUrl.setHorizontalAlignment( SwingConstants.CENTER );
		lblUrl.setForeground( Color.BLUE.darker() );
		lblUrl.setFont( FONT.deriveFont( Font.ITALIC ) );
		lblUrl.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
		lblUrl.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final java.awt.event.MouseEvent e )
			{
				try
				{
					Desktop.getDesktop().browse( new URI( DOC1_URL ) );
				}
				catch ( URISyntaxException | IOException ex )
				{
					ex.printStackTrace();
				}
			}

			@Override
			public void mouseExited( final java.awt.event.MouseEvent e )
			{
				lblUrl.setText( text );
			}

			@Override
			public void mouseEntered( final java.awt.event.MouseEvent e )
			{
				lblUrl.setText( "<html><a href=''>" + DOC1_URL + "</a></html>" );
			}
		} );
		final GridBagConstraints gbcLblUrl = new GridBagConstraints();
		gbcLblUrl.fill = GridBagConstraints.HORIZONTAL;
		gbcLblUrl.gridwidth = 3;
		gbcLblUrl.insets = new Insets( 0, 10, 5, 15 );
		gbcLblUrl.gridx = 0;
		gbcLblUrl.gridy = 3;
		add( lblUrl, gbcLblUrl );

		/*
		 * Path to Python or Cellpose.
		 */

		final JLabel lblCusstomModelFile = new JLabel( "Path to Cellpose / Python executable:" );
		lblCusstomModelFile.setFont( FONT );
		final GridBagConstraints gbcLblCusstomModelFile = new GridBagConstraints();
		gbcLblCusstomModelFile.gridwidth = 2;
		gbcLblCusstomModelFile.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblCusstomModelFile.insets = new Insets( 0, 5, 5, 5 );
		gbcLblCusstomModelFile.gridx = 0;
		gbcLblCusstomModelFile.gridy = 4;
		add( lblCusstomModelFile, gbcLblCusstomModelFile );

		btnBrowseCellposePath = new JButton( "Browse" );
		btnBrowseCellposePath.setFont( FONT );
		final GridBagConstraints gbc_btnBrowseCellposePath = new GridBagConstraints();
		gbc_btnBrowseCellposePath.insets = new Insets( 0, 5, 5, 5 );
		gbc_btnBrowseCellposePath.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnBrowseCellposePath.gridx = 2;
		gbc_btnBrowseCellposePath.gridy = 4;
		add( btnBrowseCellposePath, gbc_btnBrowseCellposePath );

		tfCellposeExecutable = new JTextField( "" );
		tfCellposeExecutable.setFont( SMALL_FONT );
		final GridBagConstraints gbcTfCellpose = new GridBagConstraints();
		gbcTfCellpose.gridwidth = 3;
		gbcTfCellpose.insets = new Insets( 0, 5, 5, 5 );
		gbcTfCellpose.fill = GridBagConstraints.BOTH;
		gbcTfCellpose.gridx = 0;
		gbcTfCellpose.gridy = 5;
		add( tfCellposeExecutable, gbcTfCellpose );
		tfCellposeExecutable.setColumns( 15 );

		/*
		 * Custom model.
		 */

		final JLabel lblPathToCustomModel = new JLabel( "Path to custom model:" );
		lblPathToCustomModel.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		final GridBagConstraints gbc_lblPathToCustomModel = new GridBagConstraints();
		gbc_lblPathToCustomModel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblPathToCustomModel.gridwidth = 2;
		gbc_lblPathToCustomModel.insets = new Insets( 0, 5, 5, 5 );
		gbc_lblPathToCustomModel.gridx = 0;
		gbc_lblPathToCustomModel.gridy = 7;
		add( lblPathToCustomModel, gbc_lblPathToCustomModel );

		btnBrowseCustomModel = new JButton( "Browse" );
		btnBrowseCustomModel.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		final GridBagConstraints gbc_btnBrowseCustomModel = new GridBagConstraints();
		gbc_btnBrowseCustomModel.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnBrowseCustomModel.insets = new Insets( 0, 0, 5, 5 );
		gbc_btnBrowseCustomModel.gridx = 2;
		gbc_btnBrowseCustomModel.gridy = 7;
		add( btnBrowseCustomModel, gbc_btnBrowseCustomModel );

		tfCustomPath = new JTextField( " " );
		tfCustomPath.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		tfCustomPath.setColumns( 15 );
		final GridBagConstraints gbc_tfCustomPath = new GridBagConstraints();
		gbc_tfCustomPath.gridwidth = 3;
		gbc_tfCustomPath.insets = new Insets( 0, 5, 5, 5 );
		gbc_tfCustomPath.fill = GridBagConstraints.BOTH;
		gbc_tfCustomPath.gridx = 0;
		gbc_tfCustomPath.gridy = 8;
		add( tfCustomPath, gbc_tfCustomPath );

		/*
		 * Pretrained model.
		 */

		final JLabel lblPretrainedModel = new JLabel( "Pretrained model:" );
		lblPretrainedModel.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblPretrainedModel = new GridBagConstraints();
		gbcLblPretrainedModel.anchor = GridBagConstraints.EAST;
		gbcLblPretrainedModel.insets = new Insets( 0, 5, 5, 5 );
		gbcLblPretrainedModel.gridx = 0;
		gbcLblPretrainedModel.gridy = 9;
		add( lblPretrainedModel, gbcLblPretrainedModel );

		cmbboxPretrainedModel = new JComboBox<>( new Vector<>( Arrays.asList( PretrainedModel.values() ) ) );
		cmbboxPretrainedModel.setFont( SMALL_FONT );
		final GridBagConstraints gbcCmbboxPretrainedModel = new GridBagConstraints();
		gbcCmbboxPretrainedModel.gridwidth = 2;
		gbcCmbboxPretrainedModel.insets = new Insets( 0, 5, 5, 5 );
		gbcCmbboxPretrainedModel.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxPretrainedModel.gridx = 1;
		gbcCmbboxPretrainedModel.gridy = 9;
		add( cmbboxPretrainedModel, gbcCmbboxPretrainedModel );

		/*
		 * Channel 1
		 */

		final JLabel lblSegmentInChannel = new JLabel( "Channel to segment:" );
		lblSegmentInChannel.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSegmentInChannel = new GridBagConstraints();
		gbcLblSegmentInChannel.anchor = GridBagConstraints.EAST;
		gbcLblSegmentInChannel.insets = new Insets( 0, 5, 5, 5 );
		gbcLblSegmentInChannel.gridx = 0;
		gbcLblSegmentInChannel.gridy = 10;
		add( lblSegmentInChannel, gbcLblSegmentInChannel );

		final List< String > l1 = Arrays.asList(
				"0: grayscale",
				"1: red",
				"2: green",
				"3: blue" );
		cmbboxCh1 = new JComboBox<>( new Vector<>( l1 ) );
		cmbboxCh1.setFont( SMALL_FONT );
		final GridBagConstraints gbcSpinner = new GridBagConstraints();
		gbcSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinner.gridwidth = 2;
		gbcSpinner.insets = new Insets( 0, 5, 5, 5 );
		gbcSpinner.gridx = 1;
		gbcSpinner.gridy = 10;
		add( cmbboxCh1, gbcSpinner );

		/*
		 * Channel 2.
		 */

		final JLabel lblSegmentInChannelOptional = new JLabel( "Optional second channel:" );
		lblSegmentInChannelOptional.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSegmentInChannelOptional = new GridBagConstraints();
		gbcLblSegmentInChannelOptional.anchor = GridBagConstraints.EAST;
		gbcLblSegmentInChannelOptional.insets = new Insets( 0, 5, 5, 5 );
		gbcLblSegmentInChannelOptional.gridx = 0;
		gbcLblSegmentInChannelOptional.gridy = 11;
		add( lblSegmentInChannelOptional, gbcLblSegmentInChannelOptional );

		final List< String > l2 = Arrays.asList(
				"0: none",
				"1: red",
				"2: green",
				"3: blue" );
		cmbboxCh2 = new JComboBox<>( new Vector<>( l2 ) );
		cmbboxCh2.setFont( SMALL_FONT );
		final GridBagConstraints gbcSpinnerCh2 = new GridBagConstraints();
		gbcSpinnerCh2.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerCh2.gridwidth = 2;
		gbcSpinnerCh2.insets = new Insets( 0, 5, 5, 5 );
		gbcSpinnerCh2.gridx = 1;
		gbcSpinnerCh2.gridy = 11;
		add( cmbboxCh2, gbcSpinnerCh2 );

		/*
		 * Diameter.
		 */

		final JLabel lblDiameter = new JLabel( "Cell diameter:" );
		lblDiameter.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblDiameter = new GridBagConstraints();
		gbcLblDiameter.anchor = GridBagConstraints.EAST;
		gbcLblDiameter.insets = new Insets( 0, 5, 5, 5 );
		gbcLblDiameter.gridx = 0;
		gbcLblDiameter.gridy = 12;
		add( lblDiameter, gbcLblDiameter );

		ftfDiameter = new JFormattedTextField( DIAMETER_FORMAT );
		ftfDiameter.setHorizontalAlignment( SwingConstants.CENTER );
		ftfDiameter.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfDiameter = new GridBagConstraints();
		gbcFtfDiameter.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfDiameter.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfDiameter.gridx = 1;
		gbcFtfDiameter.gridy = 12;
		add( ftfDiameter, gbcFtfDiameter );

		final JLabel lblSpaceUnits = new JLabel( model.getSpaceUnits() );
		lblSpaceUnits.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSpaceUnits = new GridBagConstraints();
		gbcLblSpaceUnits.insets = new Insets( 0, 5, 5, 5 );
		gbcLblSpaceUnits.gridx = 2;
		gbcLblSpaceUnits.gridy = 12;
		add( lblSpaceUnits, gbcLblSpaceUnits );

		chckbxUseGPU = new JCheckBox( "Use GPU:" );
		chckbxUseGPU.setHorizontalTextPosition( SwingConstants.LEFT );
		chckbxUseGPU.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxUseGPU = new GridBagConstraints();
		gbcChckbxUseGPU.anchor = GridBagConstraints.EAST;
		gbcChckbxUseGPU.gridwidth = 2;
		gbcChckbxUseGPU.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxUseGPU.gridx = 0;
		gbcChckbxUseGPU.gridy = 13;
		add( chckbxUseGPU, gbcChckbxUseGPU );

		chckbxSimplify = new JCheckBox( "Simplify contours:" );
		chckbxSimplify.setHorizontalTextPosition( SwingConstants.LEFT );
		chckbxSimplify.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxSimplify = new GridBagConstraints();
		gbcChckbxSimplify.anchor = GridBagConstraints.EAST;
		gbcChckbxSimplify.gridwidth = 2;
		gbcChckbxSimplify.insets = new Insets( 0, 5, 5, 5 );
		gbcChckbxSimplify.gridx = 0;
		gbcChckbxSimplify.gridy = 14;
		add( chckbxSimplify, gbcChckbxSimplify );

		final JLabelLogger labelLogger = new JLabelLogger();
		final GridBagConstraints gbcLabelLogger = new GridBagConstraints();
		gbcLabelLogger.anchor = GridBagConstraints.NORTH;
		gbcLabelLogger.gridwidth = 3;
		gbcLabelLogger.gridx = 0;
		gbcLabelLogger.gridy = 17;
		add( labelLogger, gbcLabelLogger );
		final Logger localLogger = labelLogger.getLogger();

		/*
		 * Preview.
		 */

		final JButton btnPreview = new JButton( "Preview", PREVIEW_ICON );
		btnPreview.setFont( FONT );
		final GridBagConstraints gbcBtnPreview = new GridBagConstraints();
		gbcBtnPreview.gridwidth = 2;
		gbcBtnPreview.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnPreview.insets = new Insets( 0, 5, 5, 5 );
		gbcBtnPreview.gridx = 1;
		gbcBtnPreview.gridy = 16;
		add( btnPreview, gbcBtnPreview );

		/*
		 * Listeners and specificities.
		 */

		btnPreview.addActionListener( e -> DetectionUtils.preview(
				model,
				settings,
				new CellposeDetectorFactory<>(),
				getSettings(),
				settings.imp.getFrame() - 1,
				localLogger,
				b -> btnPreview.setEnabled( b ) ) );

		final ItemListener l3 = e -> {
			final boolean isCustom = cmbboxPretrainedModel.getSelectedItem() == PretrainedModel.CUSTOM;
			tfCustomPath.setVisible( isCustom );
			lblPathToCustomModel.setVisible( isCustom );
			btnBrowseCustomModel.setVisible( isCustom );
		};
		cmbboxPretrainedModel.addItemListener( l3 );
		l3.itemStateChanged( null );

		btnBrowseCellposePath.addActionListener( l -> browseCellposePath() );
		btnBrowseCustomModel.addActionListener( l -> browseCustomModelPath() );
	}

	protected void browseCustomModelPath()
	{
		btnBrowseCustomModel.setEnabled( false );
		try
		{
			final File file = FileChooser.chooseFile( this, tfCustomPath.getText(), null,
					"Browse to a Cellpose custom model", DialogType.LOAD, SelectionMode.FILES_ONLY );
			if ( file != null )
				tfCustomPath.setText( file.getAbsolutePath() );
		}
		finally
		{
			btnBrowseCustomModel.setEnabled( true );
		}
	}

	protected void browseCellposePath()
	{
		btnBrowseCellposePath.setEnabled( false );
		try
		{
			final File file = FileChooser.chooseFile( this, tfCellposeExecutable.getText(), null,
					"Browse to the Cellpose Python executable", DialogType.LOAD, SelectionMode.FILES_ONLY );
			if ( file != null )
				tfCellposeExecutable.setText( file.getAbsolutePath() );
		}
		finally
		{
			btnBrowseCellposePath.setEnabled( true );
		}
	}

	@Override
	public void setSettings( final Map< String, Object > settings )
	{
		tfCellposeExecutable.setText( ( String ) settings.get( KEY_CELLPOSE_PYTHON_FILEPATH ) );
		tfCustomPath.setText( ( String ) settings.get( KEY_CELLPOSE_CUSTOM_MODEL_FILEPATH ) );
		cmbboxPretrainedModel.setSelectedItem( settings.get( KEY_CELLPOSE_MODEL ) );
		cmbboxCh1.setSelectedIndex( ( int ) settings.get( KEY_TARGET_CHANNEL ) );
		cmbboxCh2.setSelectedIndex( ( int ) settings.get( KEY_OPTIONAL_CHANNEL_2 ) );
		ftfDiameter.setValue( settings.get( KEY_CELL_DIAMETER ) );
		chckbxUseGPU.setSelected( ( boolean ) settings.get( KEY_USE_GPU ) );
		chckbxSimplify.setSelected( ( boolean ) settings.get( KEY_SIMPLIFY_CONTOURS ) );
	}

	@Override
	public Map< String, Object > getSettings()
	{
		final HashMap< String, Object > settings = new HashMap<>( 9 );

		settings.put( KEY_CELLPOSE_PYTHON_FILEPATH, tfCellposeExecutable.getText() );
		settings.put( KEY_CELLPOSE_CUSTOM_MODEL_FILEPATH, tfCustomPath.getText() );
		settings.put( KEY_CELLPOSE_MODEL, cmbboxPretrainedModel.getSelectedItem() );

		settings.put( KEY_TARGET_CHANNEL, cmbboxCh1.getSelectedIndex() );
		settings.put( KEY_OPTIONAL_CHANNEL_2, cmbboxCh2.getSelectedIndex() );

		final double diameter = ( ( Number ) ftfDiameter.getValue() ).doubleValue();
		settings.put( KEY_CELL_DIAMETER, diameter );
		settings.put( KEY_SIMPLIFY_CONTOURS, chckbxSimplify.isSelected() );
		settings.put( KEY_USE_GPU, chckbxUseGPU.isSelected() );

		settings.put( KEY_LOGGER, logger );

		return settings;
	}

	@Override
	public void clean()
	{}
}

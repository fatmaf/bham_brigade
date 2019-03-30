

public final class HeatmapOuterClass {
  private HeatmapOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }

	private static final com.google.protobuf.Descriptors.Descriptor
			internal_static_Heatmap_descriptor;
	private static final
	com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
			internal_static_Heatmap_fieldAccessorTable;
	private static com.google.protobuf.Descriptors.FileDescriptor
			descriptor;

	static {
		String[] descriptorData = {
				"\n\026protobuf/heatmap.proto\"|\n\007Heatmap\022\014\n\004t" +
						"ime\030\001 \001(\r\022\017\n\007max_lat\030\002 \001(\r\022\020\n\010max_long\030\003" +
						" \001(\r\022\017\n\007min_lat\030\004 \001(\r\022\020\n\010min_long\030\005 \001(\r\022" +
						"\014\n\004size\030\006 \003(\005\022\017\n\003map\030\007 \003(\001B\002\020\001b\006proto3"
		};
		com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
				new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
					public com.google.protobuf.ExtensionRegistry assignDescriptors(
							com.google.protobuf.Descriptors.FileDescriptor root) {
						descriptor = root;
						return null;
					}
				};
		com.google.protobuf.Descriptors.FileDescriptor
				.internalBuildGeneratedFileFrom(descriptorData,
						new com.google.protobuf.Descriptors.FileDescriptor[]{
						}, assigner);
		internal_static_Heatmap_descriptor =
				getDescriptor().getMessageTypes().get(0);
		internal_static_Heatmap_fieldAccessorTable = new
				com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
				internal_static_Heatmap_descriptor,
				new String[]{"Time", "MaxLat", "MaxLong", "MinLat", "MinLong", "Size", "Map",});
	}

	public static com.google.protobuf.Descriptors.FileDescriptor
	getDescriptor() {
		return descriptor;
	}

	public interface HeatmapOrBuilder extends
			// @@protoc_insertion_point(interface_extends:Heatmap)
			com.google.protobuf.MessageOrBuilder {

		/**
		 * <code>uint32 time = 1;</code>
		 */
		int getTime();

		/**
		 * <code>uint32 max_lat = 2;</code>
		 */
		int getMaxLat();

		/**
		 * <code>uint32 max_long = 3;</code>
		 */
		int getMaxLong();

		/**
		 * <code>uint32 min_lat = 4;</code>
		 */
		int getMinLat();

		/**
		 * <code>uint32 min_long = 5;</code>
		 */
		int getMinLong();

		/**
		 * <code>repeated int32 size = 6;</code>
		 */
		java.util.List<Integer> getSizeList();

		/**
		 * <code>repeated int32 size = 6;</code>
		 */
		int getSizeCount();

		/**
		 * <code>repeated int32 size = 6;</code>
		 */
		int getSize(int index);

		/**
		 * <pre>
		 * stores the map as a linear array that needs to be unpacked
		 * </pre>
		 *
		 * <code>repeated double map = 7 [packed = true];</code>
		 */
		java.util.List<Double> getMapList();

		/**
		 * <pre>
		 * stores the map as a linear array that needs to be unpacked
		 * </pre>
		 *
		 * <code>repeated double map = 7 [packed = true];</code>
		 */
		int getMapCount();

		/**
		 * <pre>
		 * stores the map as a linear array that needs to be unpacked
		 * </pre>
		 *
		 * <code>repeated double map = 7 [packed = true];</code>
		 */
		double getMap(int index);
	}

	/**
	 * <pre>
	 * import "google/protobuf/timestamp.proto";
	 * </pre>
	 * <p>
	 * Protobuf type {@code Heatmap}
	 */
	public static final class Heatmap extends
			com.google.protobuf.GeneratedMessageV3 implements
			// @@protoc_insertion_point(message_implements:Heatmap)
			HeatmapOrBuilder {
		public static final int TIME_FIELD_NUMBER = 1;
		public static final int MAX_LAT_FIELD_NUMBER = 2;
		public static final int MAX_LONG_FIELD_NUMBER = 3;
		public static final int MIN_LAT_FIELD_NUMBER = 4;
		public static final int MIN_LONG_FIELD_NUMBER = 5;
		public static final int SIZE_FIELD_NUMBER = 6;
		public static final int MAP_FIELD_NUMBER = 7;
		private static final long serialVersionUID = 0L;
		// @@protoc_insertion_point(class_scope:Heatmap)
		private static final Heatmap DEFAULT_INSTANCE;
		private static final com.google.protobuf.Parser<Heatmap>
				PARSER = new com.google.protobuf.AbstractParser<Heatmap>() {
			@Override
			public Heatmap parsePartialFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new Heatmap(input, extensionRegistry);
			}
		};

		static {
			DEFAULT_INSTANCE = new Heatmap();
		}

		private int bitField0_;
		private int time_;
		private int maxLat_;
		private int maxLong_;
		private int minLat_;
		private int minLong_;
		private java.util.List<Integer> size_;
		private int sizeMemoizedSerializedSize = -1;
		private java.util.List<Double> map_;
		private int mapMemoizedSerializedSize = -1;
		private byte memoizedIsInitialized = -1;

		// Use Heatmap.newBuilder() to construct.
		private Heatmap(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
			super(builder);
		}

		private Heatmap(
				com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			this();
			if (extensionRegistry == null) {
				throw new NullPointerException();
			}
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields =
					com.google.protobuf.UnknownFieldSet.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
						case 0:
							done = true;
							break;
						case 8: {

							time_ = input.readUInt32();
							break;
						}
						case 16: {

							maxLat_ = input.readUInt32();
							break;
						}
						case 24: {

							maxLong_ = input.readUInt32();
							break;
						}
						case 32: {

							minLat_ = input.readUInt32();
							break;
						}
						case 40: {

							minLong_ = input.readUInt32();
							break;
						}
						case 48: {
							if (!((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
								size_ = new java.util.ArrayList<Integer>();
								mutable_bitField0_ |= 0x00000020;
							}
							size_.add(input.readInt32());
							break;
						}
						case 50: {
							int length = input.readRawVarint32();
							int limit = input.pushLimit(length);
							if (!((mutable_bitField0_ & 0x00000020) == 0x00000020) && input.getBytesUntilLimit() > 0) {
								size_ = new java.util.ArrayList<Integer>();
								mutable_bitField0_ |= 0x00000020;
							}
							while (input.getBytesUntilLimit() > 0) {
								size_.add(input.readInt32());
							}
							input.popLimit(limit);
							break;
						}
						case 57: {
							if (!((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
								map_ = new java.util.ArrayList<Double>();
								mutable_bitField0_ |= 0x00000040;
							}
							map_.add(input.readDouble());
							break;
						}
						case 58: {
							int length = input.readRawVarint32();
							int limit = input.pushLimit(length);
							if (!((mutable_bitField0_ & 0x00000040) == 0x00000040) && input.getBytesUntilLimit() > 0) {
								map_ = new java.util.ArrayList<Double>();
								mutable_bitField0_ |= 0x00000040;
							}
							while (input.getBytesUntilLimit() > 0) {
								map_.add(input.readDouble());
							}
							input.popLimit(limit);
							break;
						}
						default: {
							if (!parseUnknownFieldProto3(
									input, unknownFields, extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(
						e).setUnfinishedMessage(this);
			} finally {
				if (((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
					size_ = java.util.Collections.unmodifiableList(size_);
				}
				if (((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
					map_ = java.util.Collections.unmodifiableList(map_);
				}
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		private Heatmap() {
			time_ = 0;
			maxLat_ = 0;
			maxLong_ = 0;
			minLat_ = 0;
			minLong_ = 0;
			size_ = java.util.Collections.emptyList();
			map_ = java.util.Collections.emptyList();
		}

		public static Heatmap parseFrom(
				java.nio.ByteBuffer data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static Heatmap parseFrom(
				java.nio.ByteBuffer data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static Heatmap parseFrom(
				com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static Heatmap parseFrom(
				com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static Heatmap parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static Heatmap parseFrom(
				byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static Heatmap parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3
					.parseWithIOException(PARSER, input);
		}

		public static Heatmap parseFrom(
				java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3
					.parseWithIOException(PARSER, input, extensionRegistry);
		}

		public static Heatmap parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3
					.parseDelimitedWithIOException(PARSER, input);
		}

		public static Heatmap parseDelimitedFrom(
				java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3
					.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
		}

		public static Heatmap parseFrom(
				com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3
					.parseWithIOException(PARSER, input);
		}

		public static Heatmap parseFrom(
				com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3
					.parseWithIOException(PARSER, input, extensionRegistry);
		}

		public static Builder newBuilder(Heatmap prototype) {
			return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
		}

		public static Heatmap getDefaultInstance() {
			return DEFAULT_INSTANCE;
		}

		public static com.google.protobuf.Parser<Heatmap> parser() {
			return PARSER;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Heatmap)) {
				return super.equals(obj);
			}
			Heatmap other = (Heatmap) obj;

			boolean result = true;
			result = result && (getTime()
					== other.getTime());
			result = result && (getMaxLat()
					== other.getMaxLat());
			result = result && (getMaxLong()
					== other.getMaxLong());
			result = result && (getMinLat()
					== other.getMinLat());
			result = result && (getMinLong()
					== other.getMinLong());
			result = result && getSizeList()
					.equals(other.getSizeList());
			result = result && getMapList()
					.equals(other.getMapList());
			result = result && unknownFields.equals(other.unknownFields);
			return result;
		}

		/**
		 * <code>uint32 time = 1;</code>
		 */
		public int getTime() {
			return time_;
		}

		/**
		 * <code>uint32 max_lat = 2;</code>
		 */
		public int getMaxLat() {
			return maxLat_;
		}

		/**
		 * <code>uint32 max_long = 3;</code>
		 */
		public int getMaxLong() {
			return maxLong_;
		}

		/**
		 * <code>uint32 min_lat = 4;</code>
		 */
		public int getMinLat() {
			return minLat_;
		}

		/**
		 * <code>uint32 min_long = 5;</code>
		 */
		public int getMinLong() {
			return minLong_;
		}

		/**
		 * <code>repeated int32 size = 6;</code>
		 */
		public java.util.List<Integer>
		getSizeList() {
			return size_;
		}

		/**
		 * <code>repeated int32 size = 6;</code>
		 */
		public int getSizeCount() {
			return size_.size();
		}

		/**
		 * <code>repeated int32 size = 6;</code>
		 */
		public int getSize(int index) {
			return size_.get(index);
		}

		/**
		 * <pre>
		 * stores the map as a linear array that needs to be unpacked
		 * </pre>
		 *
		 * <code>repeated double map = 7 [packed = true];</code>
		 */
		public java.util.List<Double>
		getMapList() {
			return map_;
		}

		/**
		 * <pre>
		 * stores the map as a linear array that needs to be unpacked
		 * </pre>
		 *
		 * <code>repeated double map = 7 [packed = true];</code>
		 */
		public int getMapCount() {
			return map_.size();
		}

		/**
		 * <pre>
		 * stores the map as a linear array that needs to be unpacked
		 * </pre>
		 *
		 * <code>repeated double map = 7 [packed = true];</code>
		 */
		public double getMap(int index) {
			return map_.get(index);
		}

		@Override
		public int hashCode() {
			if (memoizedHashCode != 0) {
				return memoizedHashCode;
			}
			int hash = 41;
			hash = (19 * hash) + getDescriptor().hashCode();
			hash = (37 * hash) + TIME_FIELD_NUMBER;
			hash = (53 * hash) + getTime();
			hash = (37 * hash) + MAX_LAT_FIELD_NUMBER;
			hash = (53 * hash) + getMaxLat();
			hash = (37 * hash) + MAX_LONG_FIELD_NUMBER;
			hash = (53 * hash) + getMaxLong();
			hash = (37 * hash) + MIN_LAT_FIELD_NUMBER;
			hash = (53 * hash) + getMinLat();
			hash = (37 * hash) + MIN_LONG_FIELD_NUMBER;
			hash = (53 * hash) + getMinLong();
			if (getSizeCount() > 0) {
				hash = (37 * hash) + SIZE_FIELD_NUMBER;
				hash = (53 * hash) + getSizeList().hashCode();
			}
			if (getMapCount() > 0) {
				hash = (37 * hash) + MAP_FIELD_NUMBER;
				hash = (53 * hash) + getMapList().hashCode();
			}
			hash = (29 * hash) + unknownFields.hashCode();
			memoizedHashCode = hash;
			return hash;
		}

		public static final com.google.protobuf.Descriptors.Descriptor
		getDescriptor() {
			return HeatmapOuterClass.internal_static_Heatmap_descriptor;
		}

		@Override
		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder() {
			return DEFAULT_INSTANCE.toBuilder();
		}

		@Override
		public Builder toBuilder() {
			return this == DEFAULT_INSTANCE
					? new Builder() : new Builder().mergeFrom(this);
		}

		@Override
		public com.google.protobuf.Parser<Heatmap> getParserForType() {
			return PARSER;
		}

		@Override
		protected FieldAccessorTable
		internalGetFieldAccessorTable() {
			return HeatmapOuterClass.internal_static_Heatmap_fieldAccessorTable
					.ensureFieldAccessorsInitialized(
							Heatmap.class, Builder.class);
		}

		@Override
		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1) return true;
			if (isInitialized == 0) return false;

			memoizedIsInitialized = 1;
			return true;
		}

		@Override
		public final com.google.protobuf.UnknownFieldSet
		getUnknownFields() {
			return this.unknownFields;
		}

		@Override
		public void writeTo(com.google.protobuf.CodedOutputStream output)
				throws java.io.IOException {
			getSerializedSize();
			if (time_ != 0) {
				output.writeUInt32(1, time_);
			}
			if (maxLat_ != 0) {
				output.writeUInt32(2, maxLat_);
			}
			if (maxLong_ != 0) {
				output.writeUInt32(3, maxLong_);
			}
			if (minLat_ != 0) {
				output.writeUInt32(4, minLat_);
			}
			if (minLong_ != 0) {
				output.writeUInt32(5, minLong_);
			}
			if (getSizeList().size() > 0) {
				output.writeUInt32NoTag(50);
				output.writeUInt32NoTag(sizeMemoizedSerializedSize);
			}
			for (int i = 0; i < size_.size(); i++) {
				output.writeInt32NoTag(size_.get(i));
			}
			if (getMapList().size() > 0) {
				output.writeUInt32NoTag(58);
				output.writeUInt32NoTag(mapMemoizedSerializedSize);
			}
			for (int i = 0; i < map_.size(); i++) {
				output.writeDoubleNoTag(map_.get(i));
			}
			unknownFields.writeTo(output);
		}

		@Override
		public int getSerializedSize() {
			int size = memoizedSize;
			if (size != -1) return size;

			size = 0;
			if (time_ != 0) {
				size += com.google.protobuf.CodedOutputStream
						.computeUInt32Size(1, time_);
			}
			if (maxLat_ != 0) {
				size += com.google.protobuf.CodedOutputStream
						.computeUInt32Size(2, maxLat_);
			}
			if (maxLong_ != 0) {
				size += com.google.protobuf.CodedOutputStream
						.computeUInt32Size(3, maxLong_);
			}
			if (minLat_ != 0) {
				size += com.google.protobuf.CodedOutputStream
						.computeUInt32Size(4, minLat_);
			}
			if (minLong_ != 0) {
				size += com.google.protobuf.CodedOutputStream
						.computeUInt32Size(5, minLong_);
			}
			{
				int dataSize = 0;
				for (int i = 0; i < size_.size(); i++) {
					dataSize += com.google.protobuf.CodedOutputStream
							.computeInt32SizeNoTag(size_.get(i));
				}
				size += dataSize;
				if (!getSizeList().isEmpty()) {
					size += 1;
					size += com.google.protobuf.CodedOutputStream
							.computeInt32SizeNoTag(dataSize);
				}
				sizeMemoizedSerializedSize = dataSize;
			}
			{
				int dataSize = 0;
				dataSize = 8 * getMapList().size();
				size += dataSize;
				if (!getMapList().isEmpty()) {
					size += 1;
					size += com.google.protobuf.CodedOutputStream
							.computeInt32SizeNoTag(dataSize);
				}
				mapMemoizedSerializedSize = dataSize;
			}
			size += unknownFields.getSerializedSize();
			memoizedSize = size;
			return size;
		}

		@Override
		protected Builder newBuilderForType(
				BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		@Override
		public Heatmap getDefaultInstanceForType() {
			return DEFAULT_INSTANCE;
		}

		/**
		 * <pre>
		 * import "google/protobuf/timestamp.proto";
		 * </pre>
		 * <p>
		 * Protobuf type {@code Heatmap}
		 */
		public static final class Builder extends
				com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
				// @@protoc_insertion_point(builder_implements:Heatmap)
				HeatmapOrBuilder {
			private int bitField0_;
			private int time_;
			private int maxLat_;
			private int maxLong_;
			private int minLat_;
			private int minLong_;
			private java.util.List<Integer> size_ = java.util.Collections.emptyList();
			private java.util.List<Double> map_ = java.util.Collections.emptyList();

			// Construct using HeatmapOuterClass.Heatmap.newBuilder()
			private Builder() {
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessageV3
						.alwaysUseFieldBuilders) {
				}
			}

			private Builder(
					BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			public static final com.google.protobuf.Descriptors.Descriptor
			getDescriptor() {
				return HeatmapOuterClass.internal_static_Heatmap_descriptor;
			}

			@Override
			public Heatmap getDefaultInstanceForType() {
				return Heatmap.getDefaultInstance();
			}

			@Override
			public Heatmap build() {
				Heatmap result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			@Override
			public Heatmap buildPartial() {
				Heatmap result = new Heatmap(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				result.time_ = time_;
				result.maxLat_ = maxLat_;
				result.maxLong_ = maxLong_;
				result.minLat_ = minLat_;
				result.minLong_ = minLong_;
				if (((bitField0_ & 0x00000020) == 0x00000020)) {
					size_ = java.util.Collections.unmodifiableList(size_);
					bitField0_ = (bitField0_ & ~0x00000020);
				}
				result.size_ = size_;
				if (((bitField0_ & 0x00000040) == 0x00000040)) {
					map_ = java.util.Collections.unmodifiableList(map_);
					bitField0_ = (bitField0_ & ~0x00000040);
				}
				result.map_ = map_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			@Override
			public Builder clone() {
				return (Builder) super.clone();
			}

			@Override
			public Builder clear() {
				super.clear();
				time_ = 0;

				maxLat_ = 0;

				maxLong_ = 0;

				minLat_ = 0;

				minLong_ = 0;

				size_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000020);
				map_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000040);
				return this;
			}

			@Override
			protected FieldAccessorTable
			internalGetFieldAccessorTable() {
				return HeatmapOuterClass.internal_static_Heatmap_fieldAccessorTable
						.ensureFieldAccessorsInitialized(
								Heatmap.class, Builder.class);
			}

			@Override
			public com.google.protobuf.Descriptors.Descriptor
			getDescriptorForType() {
				return HeatmapOuterClass.internal_static_Heatmap_descriptor;
			}

			@Override
			public Builder setField(
					com.google.protobuf.Descriptors.FieldDescriptor field,
					Object value) {
				return (Builder) super.setField(field, value);
			}

			@Override
			public Builder clearField(
					com.google.protobuf.Descriptors.FieldDescriptor field) {
				return (Builder) super.clearField(field);
			}

			@Override
			public Builder clearOneof(
					com.google.protobuf.Descriptors.OneofDescriptor oneof) {
				return (Builder) super.clearOneof(oneof);
			}

			@Override
			public Builder setRepeatedField(
					com.google.protobuf.Descriptors.FieldDescriptor field,
					int index, Object value) {
				return (Builder) super.setRepeatedField(field, index, value);
			}

			@Override
			public Builder addRepeatedField(
					com.google.protobuf.Descriptors.FieldDescriptor field,
					Object value) {
				return (Builder) super.addRepeatedField(field, value);
			}

			@Override
			public final Builder setUnknownFields(
					final com.google.protobuf.UnknownFieldSet unknownFields) {
				return super.setUnknownFieldsProto3(unknownFields);
			}

			@Override
			public final Builder mergeUnknownFields(
					final com.google.protobuf.UnknownFieldSet unknownFields) {
				return super.mergeUnknownFields(unknownFields);
			}

			@Override
			public final boolean isInitialized() {
				return true;
			}

			@Override
			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof Heatmap) {
					return mergeFrom((Heatmap) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(Heatmap other) {
				if (other == Heatmap.getDefaultInstance()) return this;
				if (other.getTime() != 0) {
					setTime(other.getTime());
				}
				if (other.getMaxLat() != 0) {
					setMaxLat(other.getMaxLat());
				}
				if (other.getMaxLong() != 0) {
					setMaxLong(other.getMaxLong());
				}
				if (other.getMinLat() != 0) {
					setMinLat(other.getMinLat());
				}
				if (other.getMinLong() != 0) {
					setMinLong(other.getMinLong());
				}
				if (!other.size_.isEmpty()) {
					if (size_.isEmpty()) {
						size_ = other.size_;
						bitField0_ = (bitField0_ & ~0x00000020);
					} else {
						ensureSizeIsMutable();
						size_.addAll(other.size_);
					}
					onChanged();
				}
				if (!other.map_.isEmpty()) {
					if (map_.isEmpty()) {
						map_ = other.map_;
						bitField0_ = (bitField0_ & ~0x00000040);
					} else {
						ensureMapIsMutable();
						map_.addAll(other.map_);
					}
					onChanged();
				}
				this.mergeUnknownFields(other.unknownFields);
				onChanged();
				return this;
			}

			private void ensureSizeIsMutable() {
				if (!((bitField0_ & 0x00000020) == 0x00000020)) {
					size_ = new java.util.ArrayList<Integer>(size_);
					bitField0_ |= 0x00000020;
				}
			}

			private void ensureMapIsMutable() {
				if (!((bitField0_ & 0x00000040) == 0x00000040)) {
					map_ = new java.util.ArrayList<Double>(map_);
					bitField0_ |= 0x00000040;
				}
			}

			@Override
			public Builder mergeFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				Heatmap parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (Heatmap) e.getUnfinishedMessage();
					throw e.unwrapIOException();
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			/**
			 * <code>uint32 time = 1;</code>
			 */
			public int getTime() {
				return time_;
			}

			/**
			 * <code>uint32 time = 1;</code>
			 */
			public Builder setTime(int value) {

				time_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>uint32 max_lat = 2;</code>
			 */
			public int getMaxLat() {
				return maxLat_;
			}

			/**
			 * <code>uint32 max_lat = 2;</code>
			 */
			public Builder setMaxLat(int value) {

				maxLat_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>uint32 max_long = 3;</code>
			 */
			public int getMaxLong() {
				return maxLong_;
			}

			/**
			 * <code>uint32 max_long = 3;</code>
			 */
			public Builder setMaxLong(int value) {

				maxLong_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>uint32 min_lat = 4;</code>
			 */
			public int getMinLat() {
				return minLat_;
			}

			/**
			 * <code>uint32 min_lat = 4;</code>
			 */
			public Builder setMinLat(int value) {

				minLat_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>uint32 min_long = 5;</code>
			 */
			public int getMinLong() {
				return minLong_;
			}

			/**
			 * <code>uint32 min_long = 5;</code>
			 */
			public Builder setMinLong(int value) {

				minLong_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>repeated int32 size = 6;</code>
			 */
			public java.util.List<Integer>
			getSizeList() {
				return java.util.Collections.unmodifiableList(size_);
			}

			/**
			 * <code>repeated int32 size = 6;</code>
			 */
			public int getSizeCount() {
				return size_.size();
			}

			/**
			 * <code>repeated int32 size = 6;</code>
			 */
			public int getSize(int index) {
				return size_.get(index);
			}

			/**
			 * <pre>
			 * stores the map as a linear array that needs to be unpacked
			 * </pre>
			 *
			 * <code>repeated double map = 7 [packed = true];</code>
			 */
			public java.util.List<Double>
			getMapList() {
				return java.util.Collections.unmodifiableList(map_);
			}

			/**
			 * <pre>
			 * stores the map as a linear array that needs to be unpacked
			 * </pre>
			 *
			 * <code>repeated double map = 7 [packed = true];</code>
			 */
			public int getMapCount() {
				return map_.size();
			}

			/**
			 * <pre>
			 * stores the map as a linear array that needs to be unpacked
			 * </pre>
			 *
			 * <code>repeated double map = 7 [packed = true];</code>
			 */
			public double getMap(int index) {
				return map_.get(index);
			}

			/**
			 * <code>uint32 time = 1;</code>
			 */
			public Builder clearTime() {

				time_ = 0;
				onChanged();
				return this;
			}

			/**
			 * <code>uint32 max_lat = 2;</code>
			 */
			public Builder clearMaxLat() {

				maxLat_ = 0;
				onChanged();
				return this;
			}

			/**
			 * <code>uint32 max_long = 3;</code>
			 */
			public Builder clearMaxLong() {

				maxLong_ = 0;
				onChanged();
				return this;
			}

			/**
			 * <code>uint32 min_lat = 4;</code>
			 */
			public Builder clearMinLat() {

				minLat_ = 0;
				onChanged();
				return this;
			}

			/**
			 * <code>uint32 min_long = 5;</code>
			 */
			public Builder clearMinLong() {

				minLong_ = 0;
				onChanged();
				return this;
			}

			/**
			 * <code>repeated int32 size = 6;</code>
			 */
			public Builder setSize(
					int index, int value) {
				ensureSizeIsMutable();
				size_.set(index, value);
				onChanged();
				return this;
			}

			/**
			 * <code>repeated int32 size = 6;</code>
			 */
			public Builder addSize(int value) {
				ensureSizeIsMutable();
				size_.add(value);
				onChanged();
				return this;
			}

			/**
			 * <code>repeated int32 size = 6;</code>
			 */
			public Builder addAllSize(
					Iterable<? extends Integer> values) {
				ensureSizeIsMutable();
				com.google.protobuf.AbstractMessageLite.Builder.addAll(
						values, size_);
				onChanged();
				return this;
			}

			/**
			 * <code>repeated int32 size = 6;</code>
			 */
			public Builder clearSize() {
				size_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000020);
				onChanged();
				return this;
			}

			/**
			 * <pre>
			 * stores the map as a linear array that needs to be unpacked
			 * </pre>
			 *
			 * <code>repeated double map = 7 [packed = true];</code>
			 */
			public Builder setMap(
					int index, double value) {
				ensureMapIsMutable();
				map_.set(index, value);
				onChanged();
				return this;
			}

			/**
			 * <pre>
			 * stores the map as a linear array that needs to be unpacked
			 * </pre>
			 *
			 * <code>repeated double map = 7 [packed = true];</code>
			 */
			public Builder addMap(double value) {
				ensureMapIsMutable();
				map_.add(value);
				onChanged();
				return this;
			}

			/**
			 * <pre>
			 * stores the map as a linear array that needs to be unpacked
			 * </pre>
			 *
			 * <code>repeated double map = 7 [packed = true];</code>
			 */
			public Builder addAllMap(
					Iterable<? extends Double> values) {
				ensureMapIsMutable();
				com.google.protobuf.AbstractMessageLite.Builder.addAll(
						values, map_);
				onChanged();
				return this;
			}

			/**
			 * <pre>
			 * stores the map as a linear array that needs to be unpacked
			 * </pre>
			 *
			 * <code>repeated double map = 7 [packed = true];</code>
			 */
			public Builder clearMap() {
				map_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000040);
				onChanged();
				return this;
			}


			// @@protoc_insertion_point(builder_scope:Heatmap)
		}

	}

  // @@protoc_insertion_point(outer_class_scope)
}